package com.example.how_much_do_you_spend;

import android.os.Handler;

public class ServiceThread extends Thread {
    Handler handler;
    boolean isRun = true;
    int waitTime;
    public ServiceThread(Handler handler, int waitTime) {
        this.handler = handler;
        this.waitTime = waitTime;
    }
    public void stopForever() {
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run() {
        //반복적으로 수행할 작업을 한다.
        while (isRun) {
            handler.sendEmptyMessage( 0 );
            //쓰레드에 있는 핸들러에게 메세지를 보냄
            try {
                Thread.sleep( waitTime ); //1000에 1초
            } catch (Exception e) { }
        }
    }
}
