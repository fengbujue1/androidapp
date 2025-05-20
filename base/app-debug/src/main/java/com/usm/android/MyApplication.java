package com.usm.android;

import android.util.Log;

import io.dcloud.application.DCloudApplication;

public class MyApplication extends DCloudApplication {


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        Log.d("MyApplication", "init: ");
        // 自己的业务实现
//        Intent intent = new Intent(this, UsmPandoraEntry.class);
//        startActivity(intent);
    }





    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}