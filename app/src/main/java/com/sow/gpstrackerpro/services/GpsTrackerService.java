package com.sow.gpstrackerpro.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.sow.gpstrackerpro.classes.Log;

import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.events.OnActivityDetected;


public class GpsTrackerService extends Service {

    private long LONG_TIME = 200 * 1000;
    private long SHORT_TIME = 80 * 1000;
    private static final String TAG = "GpsService";
    private PendingIntent pendingIntent;
    private BroadcastReceiver broadcastReceiver;
    private AlarmManager alarmManager;
    private MyApplication myApplication;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate()");
        try {
            myApplication = (MyApplication) getApplicationContext();

            myApplication.getEventActivityDetected().setOnEventListener(new OnActivityDetected() {
                @Override
                public void onEvent(String userActivity, int confidence) {
                    String old_activity = myApplication.getGpsManager().getState();
                    myApplication.getGpsManager().setState(userActivity);
                    myApplication.getGpsManager().setConfidence(String.valueOf(confidence));
                    if (!userActivity.equals("Still"))
                        if (!userActivity.equals(old_activity)) {
                            if (!myApplication.getGpsManager().isRequestingLocationUpdates()) {
                                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 500, pendingIntent);
                            }
                        }
                }
            });

            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GpsWakelock");

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context myContext, Intent i) {
                    Log.i(TAG, "broadcastReceiver.onReceive()");
                    try {
                        wakeLock.acquire();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                wakeLock.release();
                            }
                        }, 20 * 1000);

                        final long time_off;
                        if (myApplication.getGpsManager() != null) {
                            switch (myApplication.getGpsManager().getState()) {
                                case "In Vehicle": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                case "On Bicycle": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                case "On Foot": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                case "Running": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                case "Still": {
                                    time_off = LONG_TIME;
                                    break;
                                }
                                case "Tilting": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                case "Walking": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                case "Unknown": {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                                default: {
                                    time_off = SHORT_TIME;
                                    break;
                                }
                            }
                        } else {
                            Log.i(TAG, "GPSManager = null, using default time");
                            time_off = SHORT_TIME;
                        }
                        Log.i(TAG, "TIME OFF: " + time_off);
                        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + time_off, pendingIntent);
                        startLocationUpdates(myApplication.getGpsManager().getState());
//                        myApplication.updateBatteryLevel();
                    } catch (Exception e) {
                        Log.e(TAG, "broadcastReceiver.onReceive(): " + e.getMessage());
                    }
                }
            };
            registerReceiver(broadcastReceiver, new IntentFilter("GpsTrackerReceiver"));
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("GpsTrackerReceiver"), 0);
            alarmManager = (AlarmManager) (getApplicationContext().getSystemService(Context.ALARM_SERVICE));
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, pendingIntent);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand()");
        try {
            return Service.START_STICKY;
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand: " + e.getMessage());
            return Service.START_NOT_STICKY;
        }
    }


    private void startLocationUpdates(String userActivity) {
        Log.i(TAG, "Activity: " + userActivity);
        Log.w(TAG, "SERVICE: startLocationUpdates()");
        try {
            myApplication.getGpsManager().startLocationUpdates();
            Log.i(TAG, "Location update is on!");
        } catch (Exception e) {
            Log.e(TAG, "startLocationUpdates: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "onDestroy()");
        try {
            stopSelf();
            super.onDestroy();
            myApplication.getGpsManager().stopLocationUpdates();
            myApplication.getGpsManager().removeGeoFences();
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.getMessage());
        }
    }
}
