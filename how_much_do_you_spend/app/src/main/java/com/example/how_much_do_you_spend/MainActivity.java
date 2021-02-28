package com.example.how_much_do_you_spend;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private DataBase.DbOpenHelper dbOpenHelper;
    private FragmentManager fragmentManager;
    private MonthFragment monthFragment;
    private WeekFragment weekFragment;
    private DayFragment dayFragment;
    private FragmentTransaction transaction;
    private BottomNavigationView bottomNavigationView;
    private TextView totalTimeText;
    private TextView wifiText;
    private TextView mobileText;
    private TextView openText;
    private Button todayButton;
    private Button showButton;
    AppInfo info = AppInfo.getInstance();
    DayManager dayManager = DayManager.getInstance();
    private String now_fragment = "day";
    private Intent serviceintent;
    private long backBtnTime = 0;
    TimeCheckHandler timeHandler = new TimeCheckHandler();
    private ServiceThread timeThread; //onCreate


    boolean granted = false;

    //활동이 시작됨
    @Override
    public void onStart() {
        super.onStart();
    }

    //내부 클래스
    class TimeCheckHandler extends Handler {
        @Override public void handleMessage(android.os.Message msg) {
            //Log.d("debug","time is going");
            info.setTime(info.getTime() + 1);
        }
    }

    //내부 클래스
    private LockScreenStateReceiver mLockScreenStateReceiver;
    class LockScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // screen is turn off
                //("Screen locked");
                timeThread.stopForever();

            } else {


                info.setCount(info.getCount() + 1);
                setDisplayData(info.getCount(),info.getWifiUsage(),info.getMobileUsage(),info.getTime());
            }
        }
    }

    //뒤로 가기 버튼을 눌렀을 때 처리 방법
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료되며, 데이터가 저장되지 않습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    //Activity가 실행을 종료, 활동이 소멸되기 전에 호출
    //사용자가 activity를 완전히 닫거나 finish() 호출 시
    @Override
    public void onDestroy() {
        Log.d("debug","onDestroy");
        unregisterReceiver(mLockScreenStateReceiver);

        dbOpenHelper.updateData(info.getCount(),
                info.getTime(),
                info.getMobileUsage(),
                info.getWifiUsage(),
                dayManager.dateToString(dayManager.getToday()));

        timeThread.stopForever();
        stopService(serviceintent);

        super.onDestroy();
    }

    //활동이 일시중지됨 상태가 될때 호출
    @Override
    public void onPause() {
        super.onPause();
        Log.d("debug", "onPause");
    }


    private void setDisplayData(int count, long  wifiUsage, long mobileUsage, int time){
        Log.d("debug","count : " + count +
                " time : " + time +
                " wifi : " + wifiUsage +
                " mobileUsage : " + mobileUsage );


        int mobileDigit = (int)(Math.log10(mobileUsage)+1);
        int wifiDigit = (int)(Math.log10(wifiUsage)+1);
        String mobileUnit = (mobileDigit>3)? ((mobileDigit>6)? ((mobileDigit>9)? " GB" : " MB") : " KB") : " Byte";
        String wifiUnit = (wifiDigit>3)? ((wifiDigit>6)? ((wifiDigit>9)? " GB" : " MB") : " KB") : " Byte";
        int mobileDiv = (mobileDigit>3)? ((mobileDigit>6)? ((mobileDigit>9)? (int)Math.pow(1024,3) : (int)Math.pow(1024,2)) : (int)Math.pow(1024,1)) : 1;
        int wifiDiv = (wifiDigit>3)? ((wifiDigit>6)? ((wifiDigit>9)? (int)Math.pow(1024,3) : (int)Math.pow(1024,2)) : (int)Math.pow(1024,1)) : 1;


        String hour = String.format("%02d",time/3600);
        String minute = String.format("%02d",(time%3600)/60);
        String second = String.format("%02d",time%60);

        totalTimeText.setText(hour + ":" + minute + ":" + second);
        mobileText.setText(((mobileUsage!=0 && mobileUsage/mobileDiv == 0)? "1" : mobileUsage/mobileDiv) + mobileUnit);
        wifiText.setText(((wifiUsage!=0 && wifiUsage/wifiDiv==0)? "1" : wifiUsage/wifiDiv ) + wifiUnit);
        openText.setText(count + " 회");
    }

    //활동생명주기 시작~포커스가 떠날때까지(전화, 다른 활동, 기기 화면 꺼짐 등)
    //일시 중지됨에서 재개됨 (onPause(멈춤)->onResume(다시시작))
    @Override
    protected void onResume(){
        Log.d("debug", "onResume");
        super.onResume();
        if(!timeThread.isRun){
            timeThread = null;
            timeThread =  new ServiceThread(timeHandler,1000);
            timeThread.start();
        }
        setDisplayData(info.getCount(),info.getWifiUsage(),info.getMobileUsage(),info.getTime());
    }

    //시스템 시작 시 한번 ** 기본 함수 **
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timeThread = new ServiceThread(timeHandler,1000);
        timeThread.start(); //앱 실행시 Background Service 실행
        SharedPreferences dataInfo = getSharedPreferences("dataInfo",MODE_PRIVATE);
        if(!dataInfo.contains("info")){
            createDataInfo(); //데이터가 없으면 만들기,,
        }

        Log.d("debug", "onCreate");

        setContentView(R.layout.activity_main);
        mobileText = (TextView)findViewById(R.id.mobileText);
        wifiText = (TextView)findViewById(R.id.wifiText);
        totalTimeText = (TextView)findViewById(R.id.totalTimeText);
        openText = (TextView)findViewById(R.id.openText);
        todayButton = (Button)findViewById(R.id.todayButton);
        showButton = (Button)findViewById(R.id.showButton);
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigationView);

        //Fragment 설정
        fragmentManager = getSupportFragmentManager();

        monthFragment = new MonthFragment();
        weekFragment = new WeekFragment();
        dayFragment = new DayFragment();


        change_fragment("day"); // 일별로 보기로 초기화


        // 일, 주, 월 보기 네비게이션 이벤트
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.navigation_day:
                        change_fragment("day");
                        break;
                    case R.id.navigation_week:
                        change_fragment("week");
                        break;
                    case R.id.navigation_month:
                        change_fragment("month");
                        break;
                }
                return true;
            }
        });


        // 오늘 날짜 보기 버튼 이벤트
        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_fragment("day");
                dayFragment.initializeDate();
                setDisplayData(info.getCount(),info.getWifiUsage(),info.getMobileUsage(),info.getTime());
            }
        });

        // 해당 날짜 데이터 사용량확인
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dayManager.getToday().equals(dayManager.nowDay) && dayManager.option == "DAY"){
                    setDisplayData(info.getCount(),info.getWifiUsage(),info.getMobileUsage(),info.getTime());
                    return;
                }

                JSONObject dataJson =null;
                JSONObject screenJson = null;

                if(dayManager.option == "WEEK"){
                    dataJson = getData(info.getPhoneNum(),dayManager.getStartWeek(dayManager.nowDay),dayManager.getEndWeek(dayManager.nowDay));
                    screenJson = getScreen(info.getPhoneNum(),dayManager.getStartWeek(dayManager.nowDay),dayManager.getEndWeek(dayManager.nowDay));

                }else{
                    dataJson = getData(info.getPhoneNum(),dayManager.dateToString(dayManager.nowDay));
                    screenJson = getScreen(info.getPhoneNum(),dayManager.dateToString(dayManager.nowDay));
                }

                int screenCount = 0;
                long mobile = 0;
                long wifi = 0;
                int time = 0;

                if(dataJson == null || screenJson == null){
                    setDisplayData(0,0,0,0);
                    return;
                }

                try {

                    if(dataJson.getBoolean("result")){
                        JSONObject data = dataJson.getJSONObject("data");
                        mobile = data.getLong("mobile");
                        wifi = data.getLong("wifi");
                        time = data.getInt("use_time");
                    }
                    if(screenJson.getBoolean("result")){
                        JSONObject data = screenJson.getJSONObject("data");
                        screenCount = data.getInt("count");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("debug",info.getPhoneNum());
                Log.d("debug",screenCount + " " + mobile + " " + wifi + " " + time);
                setDisplayData(screenCount,wifi,mobile,time);
            }
        });

        //권한 요청 페이지로 이동
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        Log.d("debug", "===== CheckPhoneState isRooting granted = " + granted);

        if (granted == false) {
            // 권한이 없을 경우 권한 요구 페이지 이동
            Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
            this.startActivity(intent);
        }
        /////////////////////////////////////////////





        // 스레드 시작
        serviceintent = new Intent( MainActivity.this, InsertService.class );
        startService( serviceintent );


        //////////////////////////  screen on/off start
        mLockScreenStateReceiver = new LockScreenStateReceiver();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        registerReceiver(mLockScreenStateReceiver, filter);

        ///// db start
        dbOpenHelper = new DataBase.DbOpenHelper(getApplicationContext());
        dbOpenHelper.open();

        if(dbOpenHelper.getLength() == 0){
            dbOpenHelper.insertColumn(0,0,0,0,dayManager.dateToString(dayManager.getToday()));
        }
        else {
            Cursor c = dbOpenHelper.getData();
            c.moveToLast();
            Log.d("debug","count : " + c.getInt(c.getColumnIndex("count")) +
                    " time : " + c.getInt(c.getColumnIndex("time")) +
                    " wifi : " + c.getLong(c.getColumnIndex("wifi"))*1024 +
                    " mobileUsage : " + c.getLong(c.getColumnIndex("mobile"))*1024 +
                    " date : " + c.getString(c.getColumnIndex("date")));

            if (!c.getString(c.getColumnIndex("date")).equals(dayManager.dateToString(dayManager.getToday()))){
                //Log.d("debug", String.valueOf(c.getString(c.getColumnIndex("date")).equals(dayManager.dateToString(dayManager.getToday()))));
                dbOpenHelper.initData();
            }

            info.setCount(c.getInt(c.getColumnIndex("count")));
            info.setTime(c.getInt(c.getColumnIndex("time")));
            info.setWifiUsage(c.getLong(c.getColumnIndex("wifi"))*1024);
            info.setMobileUsage(c.getLong(c.getColumnIndex("mobile"))*1024);
        }

        setDisplayData(info.getCount(),info.getWifiUsage(),info.getMobileUsage(),info.getTime());
    }

    // 프래그먼트 변경 ( 일, 월, 주 )
    void change_fragment(String fragment){
        switch (fragment){
            case "day":
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frameLayout,dayFragment).commitAllowingStateLoss();
                now_fragment = "day";
                break;
            case "week":
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frameLayout,weekFragment).commitAllowingStateLoss();
                now_fragment = "week";
                break;
            case "month":
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frameLayout,monthFragment).commitAllowingStateLoss();
                now_fragment = "month";
                break;
        }
    }

    public JSONObject getData(String phoneNum, String dataDate){
        JSONObject res_obj=null;
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // req body json
        String func = dayManager.option.toLowerCase();
        try {
            //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
            res_obj = new JSONObject(reqToServer.execute("GET", "data/"+func+"?phoneNum="+phoneNum+"&dataDate="+dataDate,String.valueOf(req_json)).get());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return res_obj;
    }

    public JSONObject getData(String phoneNum, String startDate, String endDate){
        JSONObject res_obj=null;
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // req body json

        try {
            //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
            res_obj = new JSONObject(reqToServer.execute("GET", "data/week?phoneNum="+phoneNum+"&startDate="+startDate +"&endDate="+endDate,String.valueOf(req_json)).get());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return res_obj;
    }
    public JSONObject getScreen(String phoneNum, String dataDate){
        JSONObject res_obj=null;
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // req body json
        String func = dayManager.option.toLowerCase();
        try {
            //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
            res_obj = new JSONObject(reqToServer.execute("GET", "screen/"+func+"?phoneNum="+phoneNum+"&screenDate="+dataDate,String.valueOf(req_json)).get());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return res_obj;
    }
    public JSONObject getScreen(String phoneNum, String startDate, String endDate){
        JSONObject res_obj=null;
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // req body json

        try {
            //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
            res_obj = new JSONObject(reqToServer.execute("GET", "screen/week?phoneNum="+phoneNum+"&startDate="+startDate+"&endDate="+endDate,String.valueOf(req_json)).get());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return res_obj;
    }

    public void createDataInfo(){
        SharedPreferences loginInfo = getSharedPreferences("dataInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = loginInfo.edit();
        String input = "";
        for(int i=0;i<24;++i){
            input += "0,";
        }

        editor.putString("info",input.substring(0,input.length()-1));
        editor.commit();
    }
}