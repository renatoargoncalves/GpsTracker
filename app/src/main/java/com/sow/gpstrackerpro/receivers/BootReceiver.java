package com.sow.gpstrackerpro.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.sow.gpstrackerpro.classes.Log;


public class BootReceiver extends BroadcastReceiver {

    private String TAG = "MyBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.w(TAG, "Boot event received.");
        }
    }

}