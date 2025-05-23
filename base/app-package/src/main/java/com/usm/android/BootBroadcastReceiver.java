package com.usm.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.dcloud.PandoraEntry;  

public class BootBroadcastReceiver extends BroadcastReceiver {  

    static final String action_boot="android.intent.action.BOOT_COMPLETED";  

    @Override  
    public void onReceive(Context context, Intent intent) {  

        if (intent.getAction().equals(action_boot)){  

            // 注意H5+SDK的Main Activity为PandoraEntry（见AndroidMainfest.xml）  
            Intent bootMainIntent = new Intent(context, PandoraEntry.class);  

            // 这里必须为FLAG_ACTIVITY_NEW_TASK  
            bootMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  

            context.startActivity(bootMainIntent);  
        }  

    }  
}