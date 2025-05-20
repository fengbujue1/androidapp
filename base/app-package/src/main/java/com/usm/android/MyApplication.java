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
    }





    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}