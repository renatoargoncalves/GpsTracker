package com.sow.gpstrackerpro.classes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sow.gpstrackerpro.MainActivity;
import com.sow.gpstrackerpro.R;
import com.sow.gpstrackerpro.application.MyApplication;

import java.util.Random;

import static android.R.attr.action;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";
    private MyApplication myApplication;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser firebaseUser;

    public GeofenceTransitionsIntentService() {

        super("GeofenceTransitionsIntentService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");

        myApplication = (MyApplication) getApplicationContext();
        firebaseUser = myApplication.getFirebaseUser();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            com.sow.gpstrackerpro.classes.Log.e(TAG, "Goefencing Error " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.i(TAG, "geofenceTransition = " + geofenceTransition + " Enter : " + Geofence.GEOFENCE_TRANSITION_ENTER + "Exit : " + Geofence.GEOFENCE_TRANSITION_EXIT);
//        String user = firebaseUser.getDisplayName();
        String action = new String();
        String place = new String();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            action = "arrived";
            place = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();

        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            action = "left";
            place = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
        } else {
            Log.e(TAG, "Error ");
        }
        myApplication.updateCheckinOnFirebase(action, place);
//        showNotification(action, place);
    }

    public void showNotification(String action, String place) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        android.support.v7.app.NotificationCompat.Builder notificationBuilder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setContentTitle("DEBUG");
        notificationBuilder.setContentText(action + " " + place);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setVibrate(new long[]{300, 500});
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        notificationBuilder.setContentIntent(pendingNotificationIntent);
        Notification notification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


}
