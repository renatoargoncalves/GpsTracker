package com.sow.gpstrackerpro;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sow.gpstrackerpro.classes.Fragments;
import com.sow.gpstrackerpro.classes.Log;
import com.sow.gpstrackerpro.classes.SharedPrefs;
import com.sow.gpstrackerpro.classes.Util;
import com.sow.gpstrackerpro.fragments.FragmentFirstExecution;
import com.sow.gpstrackerpro.fragments.FragmentSignIn;

import static com.sow.gpstrackerpro.R.id.map;
import static com.sow.gpstrackerpro.R.id.relativeLayout_activity_initial;
import static com.sow.gpstrackerpro.classes.Fragments.SIGN_IN_REQUEST;

public class InitialActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, FragmentFirstExecution.OnFragmentFirstExecutionFinishes, FragmentSignIn.OnFragmentSignInFinishes, LocationListener {

    private static final String TAG = "InitialActivity";
    private static final int REQUEST_LOCATION_PERMITION = 112;
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FragmentManager fm = getSupportFragmentManager();
    private FragmentTransaction ft = fm.beginTransaction();
    private FragmentFirstExecution fragmentFirstExecution;
    private FragmentSignIn fragmentSignIn;
    private boolean requestingLocationUpdates;
    private LocationRequest mLocationRequest;

    @Override
    public void onClick(View v) {
        Log.w(TAG, "onClick()");
        switch (v.getId()) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        setupFirebase();

        setupUi();
    }

    private void setupFirebase() {
        Log.w(TAG, "setupFirebase()");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getEmail());

                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rlFragmentContainer);
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                    alphaAnimation.setDuration(350);
                    relativeLayout.startAnimation(alphaAnimation);
                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            getSupportFragmentManager().beginTransaction().remove(fragmentSignIn).commit();
                            Toast.makeText(InitialActivity.this, "User: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void setupUi() {
        Log.w(TAG, "setupUi()");
        toolbar = (Toolbar) findViewById(R.id.toolbar_initial);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.w(TAG, "onMapReady()");
        try {
            mMap = googleMap;
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    Handler askPermissionHandler = new Handler();
                    askPermissionHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMap.setOnMapLoadedCallback(null);
                            if (SharedPrefs.isFirstExecutionAfterInstall(InitialActivity.this)) {
                                setupFragment(Fragments.FIRST_EXECUTION.toString());
                            } else {
                                // TODO else
                            }
                        }
                    }, 1000);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onMapReady(): " + e.getMessage());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "onConnected()");

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    private void connectGoogleApiClient() {
        Log.w(TAG, "connectGoogleApiClient()");

        if (mGoogleApiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void checkLocationPermissions() {
        Log.w(TAG, "checkLocationPermissions()");
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(InitialActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InitialActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMITION);
            } else {
                connectGoogleApiClient();
            }
        } catch (Exception e) {
            Toast.makeText(this, "checkLocationPermissions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.w(TAG, "onRequestPermissionsResult()");
        try {
            switch (requestCode) {
                case REQUEST_LOCATION_PERMITION: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            checkLocationPermissions();
                            return;
                        }
                        connectGoogleApiClient();
                    } else {
                        Toast.makeText(this, "Sorry, we need location access...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "onRequestPermissionsResult: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        Log.w(TAG, "onStart()");
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        Log.w(TAG, "onStop()");
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupFragment(String fragmentName) {
        Log.w(TAG, "setupFragment()");

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        int i = getResources().getIdentifier(fragmentName, "layout", getPackageName());

        if (fragmentName.equals(Fragments.FIRST_EXECUTION.toString())) {
            fragmentFirstExecution = new FragmentFirstExecution();
            ft.add(R.id.rlFragmentContainer, fragmentFirstExecution);

            final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rlFragmentContainer);

            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(350);
            relativeLayout.startAnimation(alphaAnimation);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    relativeLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        } else if (fragmentName.equals(SIGN_IN_REQUEST.toString())) {
            fragmentSignIn = new FragmentSignIn();
            ft.add(R.id.rlFragmentContainer, fragmentSignIn);

            final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rlFragmentContainer);

            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(350);
            relativeLayout.startAnimation(alphaAnimation);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    relativeLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }

        ft.commit();

    }

    @Override
    public void onFragmentFirstExecutionFinishes(Fragments fragmentName) {
        Log.w(TAG, "onFragmentFirstExecutionFinishes()");
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rlFragmentContainer);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(350);
        relativeLayout.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction().remove(fragmentFirstExecution).commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Handler handlerAccessLocationPermission = new Handler();
        handlerAccessLocationPermission.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLocationPermissions();
            }
        }, 350);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.w(TAG, "onLocationChanged()");
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_unknown_user), Util.pxToDp(this, 54), Util.pxToDp(this, 70), false);

        mMap.clear();

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .visible(true)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17);
        mMap.animateCamera(cameraUpdate);
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mMap.setOnCameraIdleListener(null);
                Handler handlerSignInRequest = new Handler();
                handlerSignInRequest.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupFragment(SIGN_IN_REQUEST.toString());
                    }
                }, 1000);
            }
        });

        stopLocationUpdates();
    }

    public void startLocationUpdates() {
        Log.w(TAG, "startLocationUpdates()");
        try {
            requestingLocationUpdates = true;

            Handler handlerStopLocationUpdates = new Handler();
            handlerStopLocationUpdates.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (requestingLocationUpdates) {
                        Log.i(TAG, "TIMEOUT: stoping location updates");
                        stopLocationUpdates();
                    }
                }
            }, 15000);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissions();
                return;
            }

            // Create userLocation request
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(400);
            mLocationRequest.setFastestInterval(400);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Start userLocation updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            Log.e(TAG, "startLocationUpdates: " + e.getMessage());
        }
    }

    public void stopLocationUpdates() {
        Log.w(TAG, "stopLocationUpdates()");
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            requestingLocationUpdates = false;
        } catch (Exception e) {
            Log.e(TAG, "stopLocationUpdates: " + e.getMessage());
        }
    }

    @Override
    public void onFragmentSignInFinishes(Fragments fragmentName) {
        Log.w(TAG, "onFragmentSignInFinishes()");

        signIn();
    }

    public void signIn() {
        Log.w(TAG, "signIn()");
        try {
            if (Util.isDataConnected(getApplicationContext())) {
                if (Util.isGpsEnabled(this)) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
//                    Snackbar.make(relativeLayout_activity_initial, R.string.no_network_try_again_later, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } else {
//                Snackbar.make(relativeLayout_activity_initial, R.string.please_turn_your_gps_on, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "signIn()" + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                } else {
//                    Snackbar.make(relativeLayout_activity_initial, R.string.please_use_gmail_account, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult()" + e.getMessage());
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.w(TAG, "firebaseAuthWithGoogle()");
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "firebaseAuthWithGoogle(): Authentication FAILED!");
                            } else {
                                Log.i(TAG, "firebaseAuthWithGoogle(): Authentication SUCCESSFUL!");
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "firebaseAuthWithGoogle()" + e.getMessage());
        }
    }

}
