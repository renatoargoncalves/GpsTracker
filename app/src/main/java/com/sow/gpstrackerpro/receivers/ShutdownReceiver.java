package com.sow.gpstrackerpro.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sow.gpstrackerpro.classes.Log;


public class ShutdownReceiver extends BroadcastReceiver {

    private String TAG = "MyShutdownReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            Log.w(TAG, "Shutdown event received.");
            context.sendBroadcast(new Intent("SHUTDOWN"));
        }
    }

}