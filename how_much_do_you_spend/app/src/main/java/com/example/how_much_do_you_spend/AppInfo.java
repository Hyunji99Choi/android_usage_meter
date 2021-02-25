package com.example.how_much_do_you_spend;

import android.net.TrafficStats;

//싱글톤, 회원정보
public class AppInfo {
    private long baseWifi;
    private long baseMobile;

    private long wifiUsage;
    private long mobileUsage;
    private int count;
    private int time;

    private String name;
    private String phoneNum;

    private static AppInfo instance;

    private AppInfo(){
        long mobileTx = TrafficStats.getMobileTxBytes();
        long mobileRx = TrafficStats.getMobileRxBytes();
        long wifiTx = TrafficStats.getTotalTxBytes() - mobileTx;
        long wifiRx = TrafficStats.getTotalRxBytes() - mobileRx;

        baseWifi = wifiTx + wifiRx;
        baseMobile = mobileTx + mobileRx;

        wifiUsage = 0;
        mobileUsage = 0;
        count = 0;
        time = 0;
    }

    public static AppInfo getInstance(){ //싱글톤
        if(instance == null){
            instance = new AppInfo();
        }
        return instance;
    }

    public long getBaseWifi() {
        return baseWifi;
    }

    public void setBaseWifi(long baseWifi) {
        this.baseWifi = baseWifi;
    }

    public long getBaseMobile() {
        return baseMobile;
    }

    public void setBaseMobile(long baseMobile) {
        this.baseMobile = baseMobile;
    }

    public long getWifiUsage() {
        return wifiUsage;
    }

    public void setWifiUsage(long wifiUsage) {
        this.wifiUsage = wifiUsage;
    }

    public long getMobileUsage() {
        return mobileUsage;
    }

    public void setMobileUsage(long mobileUsage) {
        this.mobileUsage = mobileUsage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
