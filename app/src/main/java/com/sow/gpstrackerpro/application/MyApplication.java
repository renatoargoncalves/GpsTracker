package com.sow.gpstrackerpro.application;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.HandlePictureActivity;
import com.sow.gpstrackerpro.MainActivity;
import com.sow.gpstrackerpro.R;
import com.sow.gpstrackerpro.SignInActivity;
import com.sow.gpstrackerpro.classes.CheckIn;
import com.sow.gpstrackerpro.classes.Fence;
import com.sow.gpstrackerpro.classes.GpsManager;
import com.sow.gpstrackerpro.classes.Log;
import com.sow.gpstrackerpro.classes.MarkerOnMap;
import com.sow.gpstrackerpro.classes.UserInfo;
import com.sow.gpstrackerpro.events.EventActivityDetected;
import com.sow.gpstrackerpro.events.EventPremiumReceived;
import com.sow.gpstrackerpro.services.GpsTrackerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.R.attr.bitmap;
import static com.sow.gpstrackerpro.R.string.delete;
import static com.sow.gpstrackerpro.R.string.update;


public class MyApplication extends Application {

    private String TAG = "MyApplication";
    private FirebaseUser firebaseUser;
    private GpsManager gpsManager;
    private EventPremiumReceived eventPremiumReceived;
    private EventActivityDetected eventActivityDetected;
    private ArrayList<MarkerOnMap> markerOnMapList = new ArrayList<>();
    private ArrayList<String> userTrackedList = new ArrayList<>();
    private ArrayList<ValueEventListener> checkinListenersList = new ArrayList<>();
    private ArrayList<ValueEventListener> userInfoListenersList = new ArrayList<>();
    private ArrayList<ValueEventListener> newPictureFlagListenersList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference dbRef_checkin;
    private FirebaseDatabase database;
    private int licences = 0;
    private DatabaseReference dbRef_userTrackedList;
    private ValueEventListener userTrackedListListener;
    private boolean appStarted = false;
    private int mainActivityReloadCounter = 0;
    private DatabaseReference dbRef_userInfo;
    private DatabaseReference dbRef_userNewPictureFlag;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate()");

        try {
            database = FirebaseDatabase.getInstance();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            mAuthListener = listenFirebaseAuthState();
            mAuth.addAuthStateListener(mAuthListener);
            appStarted = false;

            if (firebaseUser != null)
                startApplication();
        } catch (Exception e) {
            Log.e(TAG, "onCreate(): " + e.getMessage());
        }
    }

    public void startApplication() {
        Log.w(TAG, "startApplication()");
        FacebookSdk.sdkInitialize(getApplicationContext());
        registerReceiver(broadcastReceiverGps, new IntentFilter("GPS_LOST"));
        registerReceiver(broadcastReceiverShutdown, new IntentFilter("SHUTDOWN"));
        updatePowerStatus(true);
        updateSignInStatus(true);
        listenForCheckIns();
        listenForBatteryLevel();
        setupEventActivityDetected();
        startGpsService();
        appStarted = true;
        eventPremiumReceived = new EventPremiumReceived();
    }

    private void listenForBatteryLevel() {

    }


    private void listenForCheckIns() {
        Log.w(TAG, "listenForCheckIns()");
        try {

            dbRef_userTrackedList = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userTrackedList/");
            userTrackedListListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "listenForCheckIns().onDataChange(): " + dataSnapshot.getValue().toString());
                        checkinListenersList = new ArrayList<>();
                        userInfoListenersList = new ArrayList<>();
                        userTrackedList = new ArrayList<>();
                        newPictureFlagListenersList = new ArrayList<>();

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            if ((boolean) dsp.getValue()) {
                                String user = dsp.getKey();
                                userTrackedList.add(user);
                                createCheckinListenerForUser(user);
                                createNewPictureFlagListenerForUser(user);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "listenForCheckIns(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "listenForCheckIns(): The read failed: " + databaseError.getCode());
                }
            };
            dbRef_userTrackedList.addValueEventListener(userTrackedListListener);
        } catch (Exception e) {
            Log.e(TAG, "listenForCheckIns(): " + e.getMessage());
        }
    }

    private void createNewPictureFlagListenerForUser(final String user) {
        Log.w(TAG, "createNewPictureFlagListenerForUser(): " + user);
        try {

            dbRef_userNewPictureFlag = database.getReference("/users/" + user + "/newPicture/");
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "createNewPictureFlagListenerForUser().onDataChange(): " + dataSnapshot.getValue());
                        if (dataSnapshot.getValue() != null) {
                            boolean flag = (boolean) dataSnapshot.getValue();
                            if (flag) {
                                deleteUserPictureFromDisk(user);
                                updateNewPictureFlagOnFirebase(false);
                            }
                        } else {
                            Log.i(TAG, "No new picture for user: "+user);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "createNewPictureFlagListenerForUser().onDataChange(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "createNewPictureFlagListenerForUser(): The read failed: " + databaseError.getCode());
                }
            };
            dbRef_userNewPictureFlag.addValueEventListener(valueEventListener);
            newPictureFlagListenersList.add(valueEventListener);
        } catch (Exception e) {
            Log.e(TAG, "createNewPictureFlagListenerForUser(): " + e.getMessage());
        }

    }

    private void updateNewPictureFlagOnFirebase(final boolean flag) {
        Log.w(TAG, "updateNewPictureFlagOnFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/newPicture/");

            ref.setValue(flag, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "updateNewPictureFlagOnFirebase(): Could not update userInfo: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "updateNewPictureFlagOnFirebase(): userInfo updated.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "updateNewPictureFlagOnFirebase(): " + e.getMessage());
        }
    }


    private void createUserInfoListenerForUser(String user) {
        Log.w(TAG, "createUserInfoListenerForUser(): " + user);
        try {

            dbRef_userInfo = database.getReference("/users/" + user + "/userInfo/");
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "createUserInfoListenerForUser().onDataChange(): " + dataSnapshot.getValue());
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        if (userInfo != null) {
                            deleteUserPictureFromDisk(userInfo.getEmail().replace(".", "!"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "createUserInfoListenerForUser().onDataChange(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "createUserInfoListenerForUser(): The read failed: " + databaseError.getCode());
                }
            };
            dbRef_userInfo.addValueEventListener(valueEventListener);
            userInfoListenersList.add(valueEventListener);
        } catch (Exception e) {
            Log.e(TAG, "createUserInfoListenerForUser(): " + e.getMessage());
        }

    }


    public void updateCheckinOnFirebase(String action, String place) {
        Log.w(TAG, "updateCheckinOnFirebase()");
        try {
            CheckIn checkIn = new CheckIn();

            Long tsLong = System.currentTimeMillis() / 1000;

            checkIn.setAction(action);
            checkIn.setPlace(place);
            checkIn.setTime(tsLong);
            checkIn.setUser(firebaseUser.getEmail().replace(".", "!"));
            checkIn.setUserName(firebaseUser.getDisplayName());

            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/checkinMsg/");
            ref.setValue(checkIn, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        com.sow.gpstrackerpro.classes.Log.i(TAG, "updateCheckinOnFirebase could not be created: " + databaseError.getMessage());
                    } else {
                        com.sow.gpstrackerpro.classes.Log.i(TAG, "updateCheckinOnFirebase created successfully.");
                    }
                }
            });


        } catch (Exception e) {
            com.sow.gpstrackerpro.classes.Log.e(TAG, "updateCheckinOnFirebase: " + e.getMessage());
        }


    }


    public void gpsDisabledEvent() {
        Log.i(TAG, "gpsDisabledEvent()");

        Intent intent = new Intent(this, SignInActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getApplicationContext());
        taskStackBuilder.addParentStack(SignInActivity.class);
        taskStackBuilder.addNextIntent(intent);

        final PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(111, PendingIntent.FLAG_UPDATE_CURRENT);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
                notificationBuilder.setContentTitle(getResources().getString(R.string.signned_out));
                notificationBuilder.setContentText(getResources().getString(R.string.please_turn_your_gps_on));
                notificationBuilder.setSmallIcon(R.drawable.ic_notification_gps_disabled);
                notificationBuilder.setVibrate(new long[]{300, 1000, 600, 1000});
                notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
                notificationBuilder.setContentIntent(pendingIntent);
                Notification notification = notificationBuilder.build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(111, notification);

            }
        }, 3000);

        signOut();
    }

    public void setupEventActivityDetected() {
        Log.w(TAG, "setupEventActivityDetected()");
        try {
            // TODO: Realmente preciso verificr o firebaseUser aqui?
            if (firebaseUser != null) {
                eventActivityDetected = new EventActivityDetected();
            } else {
                Log.i(TAG, "setupEventActivityDetected(): NULL");
            }
        } catch (Exception e) {
            Log.e(TAG, "setupEventActivityDetected(): " + e.getMessage());
        }
    }

    private void startGpsService() {
        Log.w(TAG, "startGpsService()");
        try {
            if (isMyServiceRunning(GpsTrackerService.class)) {
                stopService(new Intent(this, GpsTrackerService.class));
            }
            gpsManager = new GpsManager(getApplicationContext());
            startService(new Intent(this, GpsTrackerService.class));
        } catch (Exception e) {
            Log.e(TAG, "startGpsService(): " + e.getMessage());
        }
    }

    private void stopGpsService() {
        Log.w(TAG, "stopGpsService()");
        try {
            if (isMyServiceRunning(GpsTrackerService.class)) {
                stopService(new Intent(this, GpsTrackerService.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "stopGpsService(): " + e.getMessage());
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "isMyServiceRunning(): " + e.getMessage());
            return false;
        }
    }

    public GpsManager getGpsManager() {
        return gpsManager;
    }

    public void setGpsManager(GpsManager gpsManager) {
        this.gpsManager = gpsManager;
    }

    public EventActivityDetected getEventActivityDetected() {
        return eventActivityDetected;
    }

    public void setEventActivityDetected(EventActivityDetected eventActivityDetected) {
        this.eventActivityDetected = eventActivityDetected;
    }

    public ArrayList<MarkerOnMap> getMarkerOnMapList() {
        return markerOnMapList;
    }

    public void setMarkerOnMapList(ArrayList<MarkerOnMap> markerOnMapList) {
        this.markerOnMapList = markerOnMapList;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    @Override
    public void onTerminate() {
        Log.w(TAG, "onTerminate()");
        super.onTerminate();

        if (userTrackedListListener != null) {
            dbRef_userTrackedList.removeEventListener(userTrackedListListener);
        }
        for (int i = 0; i < checkinListenersList.size(); i++) {
            if (checkinListenersList.get(i) != null) {
                dbRef_checkin.removeEventListener(checkinListenersList.get(i));
            }
        }
        for (int i = 0; i < userInfoListenersList.size(); i++) {
            if (userInfoListenersList.get(i) != null) {
                dbRef_userInfo.removeEventListener(userInfoListenersList.get(i));
            }
        }
        for (int i = 0; i < newPictureFlagListenersList.size(); i++) {
            if (newPictureFlagListenersList.get(i) != null) {
                dbRef_userInfo.removeEventListener(newPictureFlagListenersList.get(i));
            }
        }

    }

    private FirebaseAuth.AuthStateListener listenFirebaseAuthState() {
        Log.w(TAG, "listenFirebaseAuthState()");
        try {
            FirebaseAuth.AuthStateListener localFirebaseAuthStateListener;
            mAuth = FirebaseAuth.getInstance();
            localFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        Log.i(TAG, "User is signed in.");
                        firebaseUser = user;
                    } else {
                        Log.i(TAG, "User is signed out.");
                        stopService(new Intent(getApplicationContext(), GpsTrackerService.class));
                        if (broadcastReceiverGps.isOrderedBroadcast())
                            unregisterReceiver(broadcastReceiverGps);
                        if (broadcastReceiverShutdown.isOrderedBroadcast())
                            unregisterReceiver(broadcastReceiverShutdown);
                        appStarted = false;
                    }
                }
            };
            return localFirebaseAuthStateListener;
        } catch (Exception e) {
            Log.e(TAG, "listenFirebaseAuthState()");
            return null;
        }
    }

    public void signOut() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "!") + "/signIn/");
            ref.setValue(false, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "signOut: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "signOut.");
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }


    private void createCheckinListenerForUser(final String user) {
        Log.w(TAG, "createCheckinListenerForUser(): " + user);
        try {

            dbRef_checkin = database.getReference("/users/" + user + "/checkinMsg/");
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "createCheckinListenerForUser().onDataChange(): " + dataSnapshot.getValue());
                        CheckIn checkIn = dataSnapshot.getValue(CheckIn.class);
                        if (checkIn != null) {
                            if (!checkIn.getUser().replace(".", "!").equals(firebaseUser.getEmail().replace(".", "!"))) {

                                if (isDifferentCheckIn(checkIn)) {
                                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(checkIn.getUser(), Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("place", checkIn.getPlace());
                                    editor.putString("action", checkIn.getAction());
                                    editor.commit();
                                    checkShowNotification(checkIn);
                                }

                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "createCheckinListenerForUser().onDataChange(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "createCheckinListenerForUser(): The read failed: " + databaseError.getCode());
                }
            };
            dbRef_checkin.addValueEventListener(valueEventListener);
            checkinListenersList.add(valueEventListener);
        } catch (Exception e) {
            Log.e(TAG, "createCheckinListenerForUser(): " + e.getMessage());
        }
    }

    private boolean isDifferentCheckIn(CheckIn checkIn) {

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(checkIn.getUser(), Context.MODE_PRIVATE);
        if (checkIn.getPlace().equals(sharedPref.getString("place", "none")) && checkIn.getAction().equals(sharedPref.getString("action", "none"))) {
            return false;
        } else {
            return true;
        }

    }

    private void checkShowNotification(final CheckIn checkIn) {
        Log.w(TAG, "checkShowNotification(): " + checkIn.getPlace());
        try {
            dbRef_checkin = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/places/" + checkIn.getPlace());
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "checkShowNotification.onDataChange: " + dataSnapshot.getValue());
                    try {
                        Fence fence = dataSnapshot.getValue(Fence.class);
                        Log.w(TAG, "checkShowNotification().onDataChange(): " + dataSnapshot.getValue().toString());
                        if (fence.isShowNotification()) {
                            showNotification(checkIn);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "checkShowNotification().onDataChange(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "checkShowNotification(): The read failed: " + databaseError.getCode());
                }
            };
            dbRef_checkin.addListenerForSingleValueEvent(valueEventListener);
        } catch (Exception e) {
            Log.e(TAG, "checkShowNotification(): " + e.getMessage());
        }

    }

    private void updatePowerStatus(boolean status) {
        Log.w(TAG, "updatePowerStatus()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "!") + "/powerOn/");
            ref.setValue(status, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "updatePowerStatus could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "updatePowerStatus updated successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "updatePowerStatus: " + e.getMessage());
        }
    }

    private void updateSignInStatus(boolean status) {
        Log.w(TAG, "updateSignInStatus()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/signIn/");

            ref.setValue(status, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "updateSignInStatus could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "updateSignInStatus updated successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "updateSignInStatus: " + e.getMessage());
        }
    }


    BroadcastReceiver broadcastReceiverGps = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                gpsDisabledEvent();
        }
    };

    BroadcastReceiver broadcastReceiverShutdown = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Shutdown event received");
            stopGpsService();
            eventActivityDetected = null;
            updatePowerStatus(false);
            onTerminate();
        }
    };


    public void showNotification(CheckIn checkIn) {
        Log.w(TAG, "showNotification(): " + checkIn.getPlace());

        int notificationId = 0;

        for (int i = 0; i < markerOnMapList.size(); i++) {
            if (markerOnMapList.get(i).getUserInfo().getEmail().replace(".", "!").equals(checkIn.getUser().replace(".", "!"))) {
                notificationId = i;
            }
        }
        Log.i(TAG, "notification id: " + notificationId);

        Intent intent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();

        b.putBoolean("checkIn_notification", true);
        b.putString("user", checkIn.getUser().replace(".", "!"));
        b.putInt("notificationId", notificationId);

        intent.putExtras(b);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String translatedAction = "";
        String soundStr;
        if (checkIn.getAction().equals("arrived")) {
            translatedAction = getString(R.string.arrived);
            soundStr = "android.resource://" + getPackageName() + "/" + R.raw.notif_1;
        } else {
            translatedAction = getString(R.string.left);
            soundStr = "android.resource://" + getPackageName() + "/" + R.raw.notif_2;
        }



        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setContentTitle(getString(R.string.app_name));
        notificationBuilder.setContentText(checkIn.getUserName() + " " + translatedAction + " " + checkIn.getPlace().split("\\+")[0]);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setVibrate(new long[]{300, 500});
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        notificationBuilder.setContentIntent(pendingNotificationIntent);
        Notification notification = notificationBuilder.build();

        notification.sound = Uri.parse(soundStr);
        notification.defaults = Notification.DEFAULT_LIGHTS;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    public boolean isAppStarted() {
        return appStarted;
    }

    public void setAppStarted(boolean appStarted) {
        this.appStarted = appStarted;
    }

    public EventPremiumReceived getEventPremiumReceived() {
        return eventPremiumReceived;
    }

    public void setEventPremiumReceived(EventPremiumReceived eventPremiumReceived) {
        this.eventPremiumReceived = eventPremiumReceived;
    }

    public int getMainActivityReloadCounter() {
        return mainActivityReloadCounter;
    }

    public void setMainActivityReloadCounter(int mainActivityReloadCounter) {
        this.mainActivityReloadCounter = mainActivityReloadCounter;
    }

    private void deleteUserPictureFromDisk(String user) {
        Log.w(TAG, "deleteUserPictureFromDisk()");
        File file = new File(Environment.getExternalStorageDirectory() + "/locator/" + user);


        try {
            if (file.exists()) {
                boolean result = file.delete();
                Log.i(TAG, "file deleted: " + result);
            }
        } catch (Exception e) {
            Log.e(TAG, "deleteUserPictureFromDisk(): " + e.getMessage());
        }
    }

}
