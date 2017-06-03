package com.sow.gpstrackerpro.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sow.gpstrackerpro.application.MyApplication;


public class GpsDisabledReceiver extends BroadcastReceiver {

    private String TAG = "GpsDisabledReceiver";
    private MyApplication myApplication;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            context.sendBroadcast(new Intent("GPS_LOST"));
        }
    }
}