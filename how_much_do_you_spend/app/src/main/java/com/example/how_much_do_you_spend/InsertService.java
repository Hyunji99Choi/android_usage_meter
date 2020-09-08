package com.example.how_much_do_you_spend;

import android.app.AppOpsManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class InsertService extends Service{
    ServiceThread networkUsageThread;
    DataBase.DbOpenHelper dbOpenHelper;
    private boolean firstCheck = true;
    private long preWifi = 0;
    private long preMobile = 0;
    boolean granted = false;
    AppInfo info = AppInfo.getInstance();
    Crawler crawler;
    String[] dataSendCheck;
    SharedPreferences dataInfo;
    DayManager dayManager = DayManager.getInstance();

    @Override public IBinder onBind(Intent intent) { return null; }

    private void setGranted(){
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
    }

    @Override
    public void onCreate() { //처음 시작
        dbOpenHelper = new DataBase.DbOpenHelper(getApplicationContext());
        dataInfo = getSharedPreferences("dataInfo",MODE_PRIVATE);
        Log.d("dubug","HERE "+dataInfo.toString());
        getDataSendCheck();
        //네트워크 사용량 계산 및 데이터 전송 스레드
        myServiceHandler handler = new myServiceHandler();
        networkUsageThread = new ServiceThread( handler ,2000);

        networkUsageThread.start();

        super.onCreate();
    }

    void getDataSendCheck(){
        dataSendCheck = dataInfo.getString("info","").split(","); // 이메일 호출, default ""반환
    }

    void setDataSendCheck(){
        SharedPreferences.Editor editor = dataInfo.edit();
        String input = "";
        for(int i=0;i<24;++i){
            input += dataSendCheck[i]+",";
        }

        editor.putString("info",input.substring(0,input.length()-1));
        editor.commit();
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    } //서비스가 종료될 때 할 작업

    public void onDestroy() {
        Log.d("debug","onDestroy");

        networkUsageThread.stopForever();
    }
    @Override
    public void onTaskRemoved(Intent intent){
        Log.d("debug","onTaskRemoved");

        dbOpenHelper.updateData(info.getCount(),
                info.getTime(),
                info.getMobileUsage(),
                info.getWifiUsage(),
                dayManager.dateToString(dayManager.getToday()));

        networkUsageThread.stopForever();
    }


    class myServiceHandler extends Handler {
        DataManager dataManager = new DataManager(getApplicationContext());


        private void sendScreenData(int count, String screenDate, int hour) throws JSONException {
            JSONObject req_json = new JSONObject();

            req_json.put("phoneNum", info.getPhoneNum());
            req_json.put("count", count);
            req_json.put("screenDate", screenDate);
            req_json.put("hour", hour);

            JSONObject rtn =  sendData("screen",req_json);
            Log.d("debug", "sending : " + rtn);
        }


        private void sendAppData(String dataDate, int hour) throws JSONException {
            JSONArray req_json_array = new JSONArray();
            JSONObject req_json;

            final List pkgAppsList = dataManager.getPackageList();

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.add(Calendar.HOUR_OF_DAY, -1);
            Calendar endCalendar = Calendar.getInstance();

            Log.d("debug","here1");

            HashMap<String, Long>  appWifiUsage = dataManager.getAllBytes(pkgAppsList,startCalendar.getTimeInMillis(),endCalendar.getTimeInMillis(), ConnectivityManager.TYPE_WIFI);
            HashMap<String, Long>  appMobileUsage = dataManager.getAllBytes(pkgAppsList,startCalendar.getTimeInMillis(),endCalendar.getTimeInMillis(), ConnectivityManager.TYPE_MOBILE);
//            HashMap<String, Long>  appPreWifiUsage = dataManager.getAllBytes(pkgAppsList,startCalendar.getTimeInMillis(),ConnectivityManager.TYPE_WIFI);
//            HashMap<String, Long>  appPreMobileUsage = dataManager.getAllBytes(pkgAppsList,startCalendar.getTimeInMillis(),ConnectivityManager.TYPE_MOBILE);
            HashMap<String, AppUsageInfo> appTimeUsage = dataManager.getUsageStatistics(0, endCalendar.getTimeInMillis());
            HashMap<String, AppUsageInfo> appTimePreUsage = dataManager.getUsageStatistics(0, startCalendar.getTimeInMillis());


            Log.d("debug","here2");
            for(String key : appTimeUsage.keySet() ) {
                req_json = new JSONObject();
                //사용량 없으면 건너뛰기
                if(appWifiUsage.get(key) == null){
                    Log.d("debug",key);
                    continue;
                }
                long preTimeUsage;
                if(!appTimePreUsage.containsKey(key)){
                    preTimeUsage = 0;
                }
                else{
                    preTimeUsage = appTimePreUsage.get(key).timeInForeground;
                }

                if(appMobileUsage.get(key) == 0 && appWifiUsage.get(key) == 0 && (appTimeUsage.get(key).timeInForeground - preTimeUsage) == 0 ){
                    continue;
                }

                //카테고리 체크
                String category = dbOpenHelper.getCategory(key);
                try {

                    if(category== null){
                        crawler = new Crawler();
                        category = crawler.execute(key).get(); // 패키지 넣어줘야함

                        if(category == null){
                            category = "basic"; //기본앱은 없는데 데이터 체크는 됨 이거 문제 해결해야할듯
                        }

                        dbOpenHelper.insertColumn(key,category);
                    }

                    PackageManager packageManager= getApplicationContext().getPackageManager();
                    String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(key, PackageManager.GET_META_DATA));

                    req_json.put("phoneNum", info.getPhoneNum());
                    req_json.put("category", category);
                    req_json.put("app", appName);
                    req_json.put("dataDate", dataDate);

//                    req_json.put("mobile", (appMobileUsage.get(key) - appPreMobileUsage.get(key) )); //지금꺼에서 기준 시간 뺀거
//                    req_json.put("wifi", (appWifiUsage.get(key) - appPreWifiUsage.get(key)));

                    req_json.put("mobile", (appMobileUsage.get(key) )); //지금꺼에서 기준 시간 뺀거
                    req_json.put("wifi", (appWifiUsage.get(key)));


                    req_json.put("useTime", (appTimeUsage.get(key).timeInForeground - preTimeUsage) / 1000);
                    req_json.put("hour", hour);

                    Log.d("debug", "packageName : " + key + " || appName : " + appName  + " || category = " + category +
                            " || usage : " + (appWifiUsage.get(key)) + " || time : " + (appTimeUsage.get(key).timeInForeground - preTimeUsage) / 1000);

                } catch (ExecutionException e) {
                    Log.d("debug", e.toString());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.d("debug", e.toString());
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("debug", e.toString());
                    e.printStackTrace();
                }

                req_json_array.put(req_json);
            }

            JSONObject rtn =  sendData("data",new JSONObject().put("data",req_json_array));
            Log.d("debug", "sending : " + rtn);
        }

        public JSONObject sendData(String url, JSONObject req_json){
            JSONObject res_obj=null;
            RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
            //JSONObject req_json = new JSONObject(); // req body json
            JSONObject responseMsg = null; // res message

            if (req_json.length() > 0) {
                try {
                    //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
                    res_obj = new JSONObject(reqToServer.execute("POST", url,String.valueOf(req_json)).get());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
            return res_obj;
        }

        @Override public void handleMessage(android.os.Message msg) {  //네트워크 스레드가 할일
            Calendar cal = Calendar.getInstance();
            int minute = cal.get(Calendar.MINUTE);
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            setGranted();

            long mobileTx = TrafficStats.getMobileTxBytes();
            long mobileRx = TrafficStats.getMobileRxBytes();
            long wifiTx = TrafficStats.getTotalTxBytes() - mobileTx;
            long wifiRx = TrafficStats.getTotalRxBytes() - mobileRx;

            if(firstCheck){
                preMobile = info.getMobileUsage();
                preWifi = info.getWifiUsage();
                firstCheck = false;
            }
            info.setMobileUsage(mobileTx + mobileRx - info.getBaseMobile() + preMobile);
            info.setWifiUsage(wifiTx + wifiRx - info.getBaseWifi() + preWifi);
//            Log.d("debug", minute+ "분");
//            Log.d("debug", "dataSendChek : " + dataSendCheck[hour-1] + "|");
            if (minute == 0 && granted && dataSendCheck[hour].equals("0")) { // 정각마다
                Log.d("debug", hour+ "시 정각");

                dataSendCheck[hour] = "1";

                if(hour == 0){
                    dataSendCheck[23] = "0";
                }else{
                    dataSendCheck[hour-1] = "0";
                }

                int tempHour = hour;
                String dateTime = dayManager.dateToString(dayManager.getToday());
                if(hour == 0){
                    dateTime = dayManager.dateToString(dayManager.prevDay(dayManager.getToday()));
                    tempHour = 24;
                }

                try {
                    sendAppData(dateTime,tempHour);
                    sendScreenData(info.getCount(),dateTime,tempHour);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setDataSendCheck();

                //JSONObject result = sendData("","","",""); ///
                //Log.d("debug", "sending : " + result);

                if(hour == 0){
                    info.setBaseMobile(mobileTx + mobileRx);
                    info.setBaseWifi(wifiTx + wifiRx);

                    dbOpenHelper.initData();
                }
                //thread.stopForever();  ///stop thread
            }
        }
    }
}