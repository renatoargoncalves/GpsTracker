package com.sow.gpstrackerpro.classes;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.application.MyApplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class GpsManager implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GpsManager";
    private static int LOCATION_INTERVAL = 2000;
    private static int FASTEST_LOCATION_INTERVAL = 5000;
    private MyApplication myApplication;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean googleServiceReady = false;
    private AlertDialog.Builder builder;
    private DatabaseReference rootRef;
    private FirebaseUser firebaseUser;
    private String state = new String("");
    private String confidence = new String("");
    private int locationCounter = 0;
    boolean requestingLocationUpdates = false;
    private Intent intent;
    private PendingIntent mGeofencePendingIntent;
    private PendingIntent pendingIntent;
    private List<Geofence> mGeofenceList;


    public GpsManager(Context context) {
        Log.w(TAG, "GpsManager()");
        try {
            this.context = context;

            myApplication = (MyApplication) context.getApplicationContext();

            firebaseUser = myApplication.getFirebaseUser();

            mGeofenceList = new ArrayList<Geofence>();

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(ActivityRecognition.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "GpsManager: " + e.getMessage());
        }
    }


    private void createGeoFences() {
        Log.w(TAG, "createGeoFences()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbRef_fences = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/places/");
            ValueEventListener geoFencesEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.i(TAG, "createGeoFences().onDataChange(): " + dataSnapshot.getValue().toString());

                        // remove geoFences and clear geoFences List for current user
                        removeGeoFences();
                        mGeofenceList.clear();

                        // creates a new geoFence for each geoPlace received and adds it on geoFences List
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            GeoPlace geoPlace = new GeoPlace();
                            geoPlace.setName(dsp.getKey());
                            geoPlace.setFence(dsp.getValue(Fence.class));
                            createGeofence(dsp.getKey(), geoPlace.getFence().getLat(), geoPlace.getFence().getLng(), geoPlace.getFence().getRadius());
                        }

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        LocationServices.GeofencingApi.addGeofences(
                                mGoogleApiClient,
                                getGeofencingRequest(),
                                getGeofencePendingIntent()
                        ).setResultCallback(new ResultCallback<Status>() {

                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    android.util.Log.i(TAG, "Saving Geofence");

                                } else {
                                    android.util.Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                            " : " + status.getStatusCode());
                                }
                            }
                        });


                    } catch (Exception e) {
                        Log.e(TAG, "createGeoFences" + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "createGeoFences(): The read failed: " + databaseError.getCode());
                }
            };
            dbRef_fences.addValueEventListener(geoFencesEventListener);

        } catch (Exception e) {
            Log.e(TAG, "placesListener(): " + e.getMessage());
        }
    }

    public void removeGeoFences() {
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());
    }

    public void createGeofence(String name, double latitude, double longitude, float radius) {
        Log.w(TAG, "createGeofence()");

        Geofence fence = new Geofence.Builder()
                .setRequestId(name)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        Log.i(TAG, "lat: "+latitude);
        Log.i(TAG, "lng: "+longitude);
        Log.i(TAG, "radius: "+radius);

        mGeofenceList.add(fence);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "onConnected()");
        try {
            googleServiceReady = true;

            intent = new Intent(context, ActivityRecognizedService.class);
            pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 3000, pendingIntent);

            createGeoFences();

        } catch (Exception e) {
            Log.e(TAG, "onConnected: " + e.getMessage());
        }
    }



    private GeofencingRequest getGeofencingRequest() {
        Log.w(TAG, "getGeofencingRequest()");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Log.w(TAG, "getGeofencePendingIntent()");
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this.context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this.context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.w(TAG, "onLocationChanged()");

        locationCounter++;
        Log.i(TAG, "locationCounter: " + locationCounter);
        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/location/");

            Calendar now = Calendar.getInstance();

            Long tsLong = now.getTimeInMillis();
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float) scale;

            UserLocation userLocation = new UserLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy(), tsLong.toString(), location.getSpeed(), this.state, this.confidence, batteryPct);

            ref.setValue(userLocation, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "Could not update desc location: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "User location updated.");
                    }
                }
            });

            if (locationCounter >= 3) {
                Log.i(TAG, "locationCounter limit: " + locationCounter);
                stopLocationUpdates();
            }

        } catch (Exception e) {
            Log.e(TAG, "onLocationChanged: " + e.getMessage());
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
        if (googleServiceReady) {
            stopLocationUpdates();
        } else {
            Log.i(TAG, "GoogleServices not ready!");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
        googleServiceReady = false;
    }


    public void startLocationUpdates() {
        Log.w(TAG, "startLocationUpdates()");
        try {

            setRequestingLocationUpdates(true);

            Handler handlerStopLocationUpdates = new Handler();
            handlerStopLocationUpdates.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRequestingLocationUpdates()) {
                        Log.i(TAG, "TIMEOUT: stoping location updates");
                        stopLocationUpdates();
                    }
                }
            }, 15000);

            Handler handlerFreeForNewLocationUpdates = new Handler();
            handlerFreeForNewLocationUpdates.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "TIMEOUT: handlerFreeForNewLocationUpdates");
                    setRequestingLocationUpdates(false);
                }
            }, 30000);

            locationCounter = 0; // <<----- resets the location counter everytime that location updates are requested

            if (googleServiceReady) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                // Create userLocation request
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(LOCATION_INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                // Start userLocation updates
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                Log.i(TAG, "GoogleServices not ready!");
            }
        } catch (Exception e) {
            Log.e(TAG, "startLocationUpdates: " + e.getMessage());
        }
    }


    public void stopLocationUpdates() {
        Log.w(TAG, "stopLocationUpdates()");
        try {
            locationCounter = 0; // <<<<<< ------ double check for location counter reset (the counter needs to be zero when not receiving updates

            if (googleServiceReady) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            } else {
                Log.i(TAG, "GoogleServices not ready!");
            }
        } catch (Exception e) {
            Log.e(TAG, "stopLocationUpdates: " + e.getMessage());
        }
    }

    public boolean isGoogleServiceReady() {
        return googleServiceReady;
    }

    public void setGoogleServiceReady(boolean googleServiceReady) {
        this.googleServiceReady = googleServiceReady;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocationRequest getmLocationRequest() {
        return mLocationRequest;
    }

    public void setmLocationRequest(LocationRequest mLocationRequest) {
        this.mLocationRequest = mLocationRequest;
    }

    public boolean isRequestingLocationUpdates() {
        return requestingLocationUpdates;
    }

    public void setRequestingLocationUpdates(boolean requestingLocationUpdates) {
        this.requestingLocationUpdates = requestingLocationUpdates;
    }

//    @Override
//    public void onReceive(Context myContext, Intent intent) {
//        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
//
//            LocationManager lm = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);
//            boolean gps_enabled = false;
//            boolean network_enabled = false;
//
//            try {
//                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            } catch (Exception ex) {
//            }
//
//            try {
//                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//            } catch (Exception ex) {
//            }
//
//            if (!gps_enabled && !network_enabled) {
//                printiStatus();
//            }
//        }
//    }


    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
