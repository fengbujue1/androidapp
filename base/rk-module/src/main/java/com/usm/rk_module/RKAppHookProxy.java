package com.usm.rk_module;

import android.app.Application;

import com.taobao.weex.common.WXException;

import io.dcloud.feature.uniapp.UniAppHookProxy;
import io.dcloud.feature.uniapp.UniSDKEngine;

public class RKAppHookProxy implements UniAppHookProxy {
    @Override
    public void onSubProcessCreate(Application application) {

    }


    @Override
    public void onCreate(Application application) {
        try {
            UniSDKEngine.registerModule("rk-module", RKModule.class, false);
            RKModule.initContext(application);
        } catch (WXException e) {
            e.printStackTrace();
        }
    }
}
