package com.example.how_much_do_you_spend;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager extends AppCompatActivity {
    NetworkStatsManager networkStatsManager;
    private Context context;

    DataManager(Context context) {
        this.context = context;
    }

    public List getPackageList(){
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List pkgAppsList = context.getPackageManager().queryIntentActivities(
                mainIntent, 0);
        return pkgAppsList;
    }

    private int getUid(Object obj){
        ResolveInfo resolveInfo = (ResolveInfo) obj;
        PackageInfo packageInfo = null;
        int appUid = 0;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    resolveInfo.activityInfo.packageName,
                    PackageManager.GET_PERMISSIONS);
            //String [] requestedPermissions = packageInfo.requestedPermissions;
            String packageName = packageInfo.packageName;

            appUid = context.getPackageManager().getApplicationInfo(packageName,PackageManager.GET_META_DATA).uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appUid;
    }

    private long getUidBytes(int appUid, int type,long startTime,long endTime){
        Long result = 0L;

        try{
            String subScriberId = getSubscriberId(context,type);
            try {
                NetworkStats networkStats = networkStatsManager.querySummary(type, subScriberId, startTime, endTime);
                do {
                    NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                    networkStats.getNextBucket(bucket);
                    if (bucket.getUid() == appUid) {
                        //rajeesh : in some devices this is immediately looping twice and the second iteration is returning correct value. So result returning is moved to the end.
                        result = (bucket.getRxBytes() + bucket.getTxBytes());
                    }
                } while (networkStats.hasNextBucket());

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            //Log.d("debug",e.toString());
        }


        return result;
    }

    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    public HashMap<String,Long> getAllBytes(List pkgAppsList,long startTime,long endTime , int type){
        HashMap<String,Long> hashMap = new HashMap<>();

        for (Object obj : pkgAppsList) {
            try{
                ResolveInfo resolveInfo = (ResolveInfo) obj;
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        resolveInfo.activityInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                String packageName = packageInfo.packageName;

                int appUid = getUid(obj);
                String appName = (String) context.getPackageManager()
                        .getApplicationLabel(context.getPackageManager()
                                .getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES));

                networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);

                long uidUsage = getUidBytes(appUid, type,startTime, endTime);
                hashMap.put(packageName,uidUsage);

                //result.addPackageName(packageName);

                //Log.d("debug",  "| appName : " + appName +  " |  appWifiUsage : " + wifiUidUsage + " |  packageName : " + packageName );

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("debug",e.toString());
                e.printStackTrace();
            }
        }

        return hashMap;
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public HashMap<String, AppUsageInfo> getUsageStatistics(long start_time, long end_time) {
        UsageEvents.Event currentEvent;
        //  List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsageInfo> map = new HashMap<>();
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager)
                context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (mUsageStatsManager != null) {
            // Get all apps data from starting time to end time
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(start_time, end_time);

            // Put these data into the map
            while (usageEvents.hasNextEvent()) {
                currentEvent = new UsageEvents.Event();
                usageEvents.getNextEvent(currentEvent);
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ||
                        currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                    //  allEvents.add(currentEvent);
                    String key = currentEvent.getPackageName();
                    if (map.get(key) == null) {
                        map.put(key, new AppUsageInfo());
                        sameEvents.put(key, new ArrayList<UsageEvents.Event>());
                    }
                    sameEvents.get(key).add(currentEvent);
                }
            }

            // Traverse through each app data which is grouped together and count launch, calculate duration
            for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
                int totalEvents = entry.getValue().size();
                if (totalEvents > 1) {
                    for (int i = 0; i < totalEvents - 1; i++) {
                        UsageEvents.Event E0 = entry.getValue().get(i);
                        UsageEvents.Event E1 = entry.getValue().get(i + 1);

                        if (E1.getEventType() == 1 || E0.getEventType() == 1) {
                            map.get(E1.getPackageName()).launchCount++;
                        }

                        if (E0.getEventType() == 1 && E1.getEventType() == 2) {
                            long diff = E1.getTimeStamp() - E0.getTimeStamp();
                            map.get(E0.getPackageName()).timeInForeground += diff;
                        }
                    }
                }

                // If First eventtype is ACTIVITY_PAUSED then added the difference of start_time and Event occuring time because the application is already running.
                if (entry.getValue().get(0).getEventType() == 2) {
                    long diff = entry.getValue().get(0).getTimeStamp() - start_time;
                    map.get(entry.getValue().get(0).getPackageName()).timeInForeground += diff;
                }

                // If Last eventtype is ACTIVITY_RESUMED then added the difference of end_time and Event occuring time because the application is still running .
                if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
                    long diff = end_time - entry.getValue().get(totalEvents - 1).getTimeStamp();
                    map.get(entry.getValue().get(totalEvents - 1).getPackageName()).timeInForeground += diff;
                }
            }
        }

        return map;

    }
}

class AppUsageInfo {

    public long timeInForeground;
    public int launchCount;

    AppUsageInfo() {
        this.timeInForeground = 0;
        this.launchCount = 0;
    }
}




