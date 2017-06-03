package com.sow.gpstrackerpro;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.plus.PlusOneButton;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.CustomFAB;
import com.sow.gpstrackerpro.classes.BubbleDrawable;
import com.sow.gpstrackerpro.classes.Fence;
import com.sow.gpstrackerpro.classes.GeoPlace;
import com.sow.gpstrackerpro.classes.Invite;
import com.sow.gpstrackerpro.classes.Log;

import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.classes.MarkerOnMap;
import com.sow.gpstrackerpro.classes.UserInfo;
import com.sow.gpstrackerpro.classes.UserLocation;
import com.sow.gpstrackerpro.classes.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


import static android.R.attr.id;
import static android.view.View.GONE;
import static android.view.View.TRANSLATION_X;
import static android.view.View.TRANSLATION_Y;
import static android.view.View.VISIBLE;
import static com.sow.gpstrackerpro.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private final static String TAG = "MainActivity";
    private final static int REQUEST_INVITE = 2121;
    private final static int PLUS_ONE_REQUEST_CODE = 1515;

    private MyApplication mApplication;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private PlusOneButton mPlusOneButton;

    private Toolbar toolbar;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private LinearLayout
            linearLayout_progress_bar_main,
            linearLayout_add_place,
            linearLayout_adView,
            linearLayout_area,
            linearLayout_savePlace;

    private RelativeLayout
            relativeLayout_highlight_add_user;

    private TextView
            textView_btn_delete_place,
            textView_btn_add_place,
            textView_area,
            textView_progressbar;

    private DatabaseReference
            dbRef_userLocation,
            dbRef_createInvite,
            dbRef_userTrackedList,
            dbRef_powerOn,
            dbRef_places,
            dbRef_signIn;

    private ValueEventListener
            userTrackedListListener,
            placesValueEventListener,
            signInValueEventListener;

    private ImageView
            imageView_satellite,
            imageView_street,
            imageView_center_me,
            imageView_show_all,
            imageView_new_version,
            imageView_show_marker_info,
            imageView_dont_show_marker_info,
            imageView_circle_add_user,
            imageView_alert;

    private TextView
            textView_nav_header_user_name,
            textView_nav_header_user_email,
            textView_nav_header_app_version;

    private boolean
            premium_user,
            signIn,
            restartedActivity,
            alerted,
            recommended;

    private boolean zoomToUser = true;
    private boolean showMarkerInfo = true;

    private String userToZoom = new String();

    private Handler handler_restartActivity;
    private Runnable runnable_restartActivity;

    private SharedPreferences sharedPref;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private int userTrackedListSize = 0;
    private int areaRadius = 100;
    private long executionsCounter = 0;

    private Dialog dialog;

    private AdView adView;
    private AdRequest adRequest;

    private ListView listView_search;
    private MenuItem menuItem_search;
    private SearchView searchView;
    private CircleOptions circleOptions_createFence;
    private SeekBar seekBar_area;
    private Circle mapCircle_createFence;
    private EditText editText_place;
    private ArrayList<MarkerOnMap> markersOnMap = new ArrayList<>();
    private ArrayList<GeoPlace> geoPlaces = new ArrayList<>();
    private CustomFAB
            mainFab,
            fab1,
            fab2,
            fab3,
            fab4,
            fab5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        mApplication = (MyApplication) getApplicationContext();
        mFirebaseUser = mApplication.getFirebaseUser();
        mAuthListener = listenFirebaseAuthState();

        connectGoogleApiClient();

        configureSharedPrefFile();

        configureUI();

        configureShutdownBroadcast();

        checkPhoneModel();

        checkInvites();

        listenPlacesChanges();

        Bundle bundleOnCreate = getIntent().getExtras();
        if (bundleOnCreate != null) {
            handleBundle(bundleOnCreate);
        }

        if (markersOnMap.size() == 0) {
            getUserTrackedList();
        }

        increaseExecutionsCounter();

    }

    private void configureShutdownBroadcast() {
        registerReceiver(broadcastReceiverShutdown, new IntentFilter("SHUTDOWN"));
    }

    private void configureSharedPrefFile() {
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }


    private void checkAppUpdates() {
        Log.w(TAG, "checkAppUpdates()");
        DatabaseReference dbRef_appVersion = database.getReference("/lastVersion/");
        dbRef_appVersion.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.i(TAG, "checkAppUpdates().onDataChange(): " + dataSnapshot.getValue().toString());
                    int lastVersion = Integer.valueOf(dataSnapshot.getValue().toString());
                    if (lastVersion > Util.getAppVersionCode(MainActivity.this)) {
                        imageView_new_version.setVisibility(VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "checkAppUpdates(): " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "checkAppUpdates(): The read failed: " + databaseError.getCode());
            }
        });
    }

    private void checkPhoneModel() {
        Log.w(TAG, "checkPhoneModel()");
        try {
            String storedPhoneModel = sharedPref.getString(getResources().getString(R.string.phone_model), "NA");
            if (storedPhoneModel.equals(Util.getDeviceName()) && executionsCounter % 20 != 0) {
                Log.i(TAG, "checkPhoneModel(): phoneModel not changed, will NOT update Firebase!");
            } else {
                updatePhoneModel();
            }
        } catch (Exception e) {
            Log.e(TAG, "checkPhoneModel(): " + e.getMessage());
        }
    }

    private void updatePhoneModel() {
        Log.w(TAG, "updatePhoneModel()");
        try {
            if (executionsCounter % 20 == 0)
                Log.i(TAG, "updatePhoneModel(): execution is multiple of 20!");
            else
                Log.i(TAG, "updatePhoneModel(): reading updatePhoneModel for the first time!");
            DatabaseReference ref = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/phoneModel/");
            ref.setValue(Util.getDeviceName(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "checkPhoneModel(): phoneModel has changed, however updated FAILED in Firebase: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "checkPhoneModel(): phoneModel has changed, updated correctly in Firebase!");
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.phone_model), Util.getDeviceName());
                        editor.commit();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "updatePhoneModel(): " + e.getMessage());
        }
    }

    private void increaseExecutionsCounter() {
        try {
            String versionCode = String.valueOf(Util.getAppVersionCode(this));

            DatabaseReference ref = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/executions/" + versionCode);

            ref.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(final MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.setValue(1);
                    } else {
                        currentData.setValue((Long) currentData.getValue() + 1);
                    }

                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        Log.d(TAG, "Firebase counter increment failed.");
                    } else {
                        executionsCounter = (long) dataSnapshot.getValue();

                        if (executionsCounter <= 5)
                            showAddUserTutorial();
                        else {
//                            showRightPanel(true);
//                            showLeftPanel(true);
//                            showMarkerInfo = true;
//                            showPlacesAndInfo();
                        }


                        Toast.makeText(MainActivity.this, "executionsCounter: " + executionsCounter, Toast.LENGTH_SHORT).show();
                    }
                }

            });


//            ref.setValue(count, new DatabaseReference.CompletionListener() {
//                @Override
//                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                    if (databaseError != null) {
//                        Log.i(TAG, "Could not update increaseExecutionsCounter: " + databaseError.getMessage());
//                    } else {
//                        Log.i(TAG, "increaseExecutionsCounter updated.");
//                    }
//                }
//            });

        } catch (Exception e) {
            Log.e(TAG, "onLocationChanged: " + e.getMessage());
        }

    }

    BroadcastReceiver broadcastReceiverShutdown = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Shutdown event received");
            finish();

        }
    };

    private void configureUI() {
        Log.w(TAG, "configureUI()");
        try {

            // Configures Toolbar
            toolbar = (Toolbar) findViewById(R.id.toolbar_main);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.app_name);
            if (premium_user)
                getSupportActionBar().setSubtitle(R.string.premium);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

            // Configures NavigationView
            navigationView = (NavigationView) findViewById(R.id.navigationView);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.item_invite_code:
                            startActivity(new Intent(MainActivity.this, InvitationCodeActivity.class));
                            break;
                        case R.id.item_manage_followers:
                            startActivity(new Intent(MainActivity.this, FollowersActivity.class));
                            break;
                        case R.id.item_manage_locals:
                            startActivity(new Intent(MainActivity.this, ManagePlacesActivity.class));
                            break;
                        case R.id.item_settings:
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            break;
//                        case R.id.item_rate:
//                            startActivity(new Intent(MainActivity.this, RateThisAppActivity.class));
//                            break;
                        case R.id.item_buy:
                            startActivity(new Intent(MainActivity.this, InAppBillingActivity.class));
                            break;
                        case R.id.item_help:
                            startActivity(new Intent(MainActivity.this, HelpActivity.class));
                            break;
                        case R.id.item_logout:
                            signOut();
                            break;
                    }
                    return true;
                }
            });

            // Configures the NavigationView Header
            View headerView = navigationView.getHeaderView(0);
            textView_nav_header_user_name = (TextView) headerView.findViewById(R.id.textView_nav_header_user_name);
            textView_nav_header_user_name.setText(mFirebaseUser.getDisplayName());
            textView_nav_header_user_email = (TextView) headerView.findViewById(R.id.textView_nav_header_user_email);
            textView_nav_header_user_email.setText(mFirebaseUser.getEmail());
            textView_nav_header_app_version = (TextView) headerView.findViewById(R.id.textView_nav_header_app_version);

            // Configures the DrawerLayout (hanburguer icon)
            drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();

            // Configures the MapFragment
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
            mapFragment.getMapAsync(this);

            // Configures the progress bar
            linearLayout_progress_bar_main = (LinearLayout) findViewById(R.id.linearLayout_progress_bar_main_activity);
            textView_progressbar = (TextView) findViewById(R.id.textView_progressbar);

            // Configures the ListView search
            listView_search = (ListView) findViewById(R.id.listView_search);
            listView_search.setVisibility(GONE);

            // Configures the seekBar "Area" --> When creating places
            seekBar_area = (SeekBar) findViewById(R.id.seekBar_area);
            seekBar_area.setMax(500);
            seekBar_area.setProgress(areaRadius);
            seekBar_area.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    areaRadius = i;
                    if (areaRadius < 100)
                        areaRadius = 100;
                    mapCircle_createFence.setRadius(areaRadius);
                    textView_area.setText(areaRadius + "m");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            // Configures the TextView delete place
            textView_btn_delete_place = (TextView) findViewById(R.id.textView_btn_delete_place);
            textView_btn_delete_place.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayout_area.setVisibility(VISIBLE);
                    linearLayout_savePlace.setVisibility(VISIBLE);
                    linearLayout_add_place.setVisibility(GONE);
                    mapCircle_createFence.remove();
                }
            });

            // Configures the TextView add place
            textView_btn_add_place = (TextView) findViewById(R.id.textView_btn_add_place);
            textView_btn_add_place.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editText_place.getText().length() > 2) {
                        for (int i = 0; i < markersOnMap.size(); i++) {
                            createPlace(
                                    markersOnMap.get(i).getUserInfo().getEmail().replace(".", "!"),
                                    editText_place.getText().toString() + "+" + mFirebaseUser.getEmail().replace(".", "!"),
                                    new Fence(areaRadius, mMap.getCameraPosition().target.latitude,
                                            mMap.getCameraPosition().target.longitude,
                                            editText_place.getText().toString() + "+" + mFirebaseUser.getEmail().replace(".", "!"),
                                            mFirebaseUser.getEmail().replace(".", "!"),
                                            true,
                                            true
                                    )
                            );
                        }
                        editText_place.setText("");
                        setupMapMoveListener();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.places_must_have_3_digits), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Configures the EditText place --> Where the user types the name of a place
            editText_place = (EditText) findViewById(R.id.editText_place);
            editText_place.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        linearLayout_area.setVisibility(GONE);
                        linearLayout_savePlace.setVisibility(GONE);
                    }
                }
            });

            // Configures the EditText place to show and hide the soft keyboard
            editText_place.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    switch (actionId) {
                        case EditorInfo.IME_ACTION_DONE:
                            InputMethodManager inputManager = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                            Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    linearLayout_area.setVisibility(VISIBLE);
                                    linearLayout_savePlace.setVisibility(VISIBLE);
                                }
                            }, 650);
                            break;
                    }
                    return true;
                }
            });

            // Configures the linearLayout add place and the components inside it
            linearLayout_add_place = (LinearLayout) findViewById(R.id.linearLayout_add_place);
            linearLayout_add_place.setVisibility(GONE);
            textView_area = (TextView) findViewById(R.id.textView_area);
            linearLayout_area = (LinearLayout) findViewById(R.id.linearLayout_area);
            linearLayout_savePlace = (LinearLayout) findViewById(R.id.linearLayout_savePlace);

            // Configures the linearLayout adView
            linearLayout_adView = (LinearLayout) findViewById(R.id.linearLayout_adView);
            linearLayout_adView.setVisibility(GONE);


            relativeLayout_highlight_add_user = (RelativeLayout) findViewById(R.id.relativeLayout_highlight_add_user);
            relativeLayout_highlight_add_user.setVisibility(View.INVISIBLE);
            imageView_circle_add_user = (ImageView) findViewById(R.id.imageView_circle_add_user);


//            // Configures the ImageView satellite  ==>  Shows the satellite view
//            imageView_satellite = (ImageView) findViewById(R.id.imageView_satellite);
//            imageView_satellite.setVisibility(GONE);
//            imageView_satellite.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    imageView_street.setVisibility(VISIBLE);
//                    imageView_satellite.setVisibility(GONE);
//                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        linearLayout_rightPanel.setBackground(null);
//                        linearLayout_leftPanel.setBackground(null);
//                    }
//                    for (int i = 0; i < geoPlaces.size(); i++) {
//                        try {
//                            geoPlaces.get(i).getCircle().setFillColor(0x77AAAAAA);
//                        } catch (Exception e) {
//                            // users choice
//                        }
//                    }
//                }
//            });

//            // Configures the ImageView street  ==>  Shows the map view
//            imageView_street = (ImageView) findViewById(R.id.imageView_street);
//            imageView_street.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    imageView_satellite.setVisibility(VISIBLE);
//                    imageView_street.setVisibility(GONE);
//                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        linearLayout_rightPanel.setBackground(getDrawable(R.drawable.places_options_bg));
//                        linearLayout_leftPanel.setBackground(getDrawable(R.drawable.places_options_bg));
//                    } else {
//                        linearLayout_rightPanel.setBackgroundColor(getResources().getColor(R.color.white));
//                        linearLayout_leftPanel.setBackgroundColor(getResources().getColor(R.color.white));
//                    }
//                    linearLayout_rightPanel.setAlpha(.8f);
//                    linearLayout_leftPanel.setAlpha(.8f);
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        linearLayout_leftPanel.setBackground(getDrawable(R.drawable.places_options_bg));
//                    } else {
//                        linearLayout_leftPanel.setBackgroundColor(getResources().getColor(R.color.white));
//                    }
//                    linearLayout_leftPanel.setAlpha(.8f);
//
//                    for (int i = 0; i < geoPlaces.size(); i++) {
//                        try {
//                            geoPlaces.get(i).getCircle().setFillColor(0xAAFFFFFF);
//                        } catch (Exception e) {
//                            // users choice
//                        }
//                    }
//                }
//            });

//            // Configures the ImageView center me  ==>  Centralizes the map on the user
//            imageView_center_me = (ImageView) findViewById(R.id.imageView_center_me);
//            imageView_center_me.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    for (int i = 0; i < markersOnMap.size(); i++) {
//                        if (markersOnMap.get(i).getUserInfo().getEmail().equals(mFirebaseUser.getEmail())) {
//                            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(markersOnMap.get(i).getMarker().getPosition(), 16);
//                            mMap.animateCamera(cu);
//                            break;
//                        }
//                    }
//                }
//            });

//            // Configures the ImageView new version  ==>  Shows up when there is a new version available
//            imageView_new_version = (ImageView) findViewById(R.id.imageView_new_version);
//            imageView_new_version.setVisibility(GONE);
//            imageView_new_version.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String APP_MARKET_URL = "market://details?id=com.sow.gpstrackerpro";
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_MARKET_URL));
//                    startActivity(intent);
//                }
//            });

//            // Configures the ImageView marker info  ==>  shows details about a user
//            imageView_show_marker_info = (ImageView) findViewById(R.id.imageView_show_marker_info);
//            imageView_show_marker_info.setVisibility(GONE);
//            imageView_show_marker_info.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    imageView_show_marker_info.setVisibility(GONE);
//                    imageView_dont_show_marker_info.setVisibility(VISIBLE);
//                    showMarkerInfo = true;
//                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, 16);
//                    mMap.animateCamera(cu);
//                    showPlacesAndInfo();
//                }
//            });

//            // Configures the ImageView marker info  ==>  hides details about a user
//            imageView_dont_show_marker_info = (ImageView) findViewById(R.id.imageView_dont_show_marker_info);
//            imageView_dont_show_marker_info.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    imageView_show_marker_info.setVisibility(VISIBLE);
//                    imageView_dont_show_marker_info.setVisibility(GONE);
//                    showMarkerInfo = false;
//                    hidePlacesAndInfo();
//                }
//            });


//            // Configures the ImageView show all users
//            imageView_show_all = (ImageView) findViewById(R.id.imageView_show_all);
//            imageView_show_all.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                    for (int i = 0; i < markersOnMap.size(); i++) {
//                        builder.include(markersOnMap.get(i).getMarker().getPosition());
//                    }
//                    LatLngBounds bounds = builder.build();
//                    int padding = 180; // offset from edges of the map in pixels
//                    CameraUpdate cu_showAll = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//                    mMap.animateCamera(cu_showAll);
//                }
//            });

//            // Configures the ImageView alert
//            imageView_alert = (ImageView) findViewById(R.id.imageView_alert);
//            imageView_alert.setVisibility(GONE);
//            imageView_alert.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showBackgroundAlert();
//                }
//            });
//            alerted = sharedPref.getBoolean(getString(R.string.checkbox_alert_background_dont_show_again), false);
//            Log.i(TAG, "alerted: " + alerted);
//            if (!alerted && executionsCounter > 3) {
//                imageView_alert.setVisibility(VISIBLE);
//            }

//            // Configures the layout rightPanel ==> the container of the icons on the right side
//            linearLayout_rightPanel = (LinearLayout) findViewById(R.id.linearLayout_rightPanel);
//            linearLayout_rightPanel.setVisibility(GONE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                linearLayout_rightPanel.setBackground(null);
//            }

//            // Configures the layout leftPanel ==> the container of the icons on the left side
//            linearLayout_leftPanel = (LinearLayout) findViewById(R.id.linearLayout_leftPanel);
//            linearLayout_leftPanel.setVisibility(GONE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                linearLayout_leftPanel.setBackground(null);
//            }

//            // Configures the Google Plus One button
//            mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
//            mPlusOneButton.setVisibility(GONE);
//            boolean liked = sharedPref.getBoolean("liked", false);
//            recommended = sharedPref.getBoolean("recommended", false);
//            if (!recommended && !liked && executionsCounter > 7) {
//                mPlusOneButton.setVisibility(VISIBLE);
//            }

            mainFab = (CustomFAB) findViewById(R.id.fab);
            mainFab.setImageDrawable(getDrawable(R.drawable.ic_more));
            mainFab.setState1((AnimatedVectorDrawable) getDrawable(R.drawable.avd_expand_fab_main_fab));
            mainFab.setState2((AnimatedVectorDrawable) getDrawable(R.drawable.avd_collapse_main_fab));
            mainFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainFab.morph();
                    if (mainFab.ismIsShowingState2())
                        expandFab();
                    else
                        collapseFab();
                }
            });

            fab1 = (CustomFAB) findViewById(R.id.fab1);
            fab1.setImageDrawable(getDrawable(R.drawable.ic_action_add_user));
            fab1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            fab2 = (CustomFAB) findViewById(R.id.fab2);
            fab2.setImageDrawable(getDrawable(R.drawable.ic_action_add_place));
            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            fab3 = (CustomFAB) findViewById(R.id.fab3);
            fab3.setImageDrawable(getDrawable(R.drawable.ic_action_add_place));
            fab3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            fab4 = (CustomFAB) findViewById(R.id.fab4);
            fab4.setImageDrawable(getDrawable(R.drawable.ic_action_add_place));
            fab4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            fab5 = (CustomFAB) findViewById(R.id.fab5);
            fab5.setImageDrawable(getDrawable(R.drawable.ic_action_add_place));
            fab5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "configureUI(): " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void expandFab() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                createExpandAnimatorUp(fab1, 220),
                createExpandAnimatorUp(fab2, 400),
                createExpandAnimatorUp(fab3, 580),
                createExpandAnimatorLeft(fab4, 220),
                createExpandAnimatorLeft(fab5, 400));
        animatorSet.start();
        animateFab();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void collapseFab() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                createCollapseAnimatorDown(fab1, 220),
                createCollapseAnimatorDown(fab2, 400),
                createCollapseAnimatorDown(fab3, 580),
                createCollapseAnimatorRight(fab4, 220),
                createCollapseAnimatorRight(fab5, 400));
        animatorSet.start();
        animateFab();
    }

    private void animateFab() {
        Drawable drawable = mainFab.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    private Animator createExpandAnimatorUp(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, -offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createCollapseAnimatorDown(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, -offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createExpandAnimatorLeft(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, 0, -offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createCollapseAnimatorRight(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, -offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private void showBackgroundAlert() {

        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_running_background_alert);

        LinearLayout linearLayout_ok = (LinearLayout) dialog.findViewById(R.id.linearLayout_ok);
        linearLayout_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.checkbox_alert_background_dont_show_again);
                if (checkBox.isChecked()) {
                    editor.putBoolean(getString(R.string.checkbox_alert_background_dont_show_again), true);
                    imageView_alert.setVisibility(GONE);
                } else {
                    editor.putBoolean(getString(R.string.checkbox_alert_background_dont_show_again), false);
                }
                editor.commit();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private String getPremium() {
        if (premium_user)
            return getString(R.string.premium);
        else
            return getString(R.string.free);
    }


    private void updateLocation(String user, UserLocation userLocation) {
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + user.replace(".", "!") + "/location/");

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

        } catch (Exception e) {
            Log.e(TAG, "onLocationChanged: " + e.getMessage());
        }


    }

    private void createPlace(String user, String place, Fence fence) {
        Log.w(TAG, "createPlace()");
        try {
            DatabaseReference ref = database.getReference("/users/" + user + "/places/" + place);
            ref.setValue(fence, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "createPlace could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "createPlace created successfully.");
                        try {
                            linearLayout_add_place.setVisibility(GONE);
                            mapCircle_createFence.remove();
                        } catch (Exception e) {

                        }
                    }
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "createPlace: " + e.getMessage());
        }

    }

    private void addPlace() {

        if (premium_user) {
            createFence();
        } else {
            checkPlacesQty();
        }
    }

    private void createFence() {
        linearLayout_add_place.setVisibility(VISIBLE);
        textView_area.setText(areaRadius + "m");
        circleOptions_createFence = new CircleOptions()
                .center(mMap.getCameraPosition().target)
                .radius(areaRadius)
                .strokeColor(0x772E2E2E)
                .strokeWidth(1.8f)
                .fillColor(0x77AAAAAA);

        mapCircle_createFence = mMap.addCircle(circleOptions_createFence);
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mapCircle_createFence.setCenter(mMap.getCameraPosition().target);
            }
        });
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
                        // User is signed in
                    } else {
                        // User is signed out
                        Log.i(TAG, "USER SIGN OUT:");
//                        onStop();

                        try {
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "listenFirebaseAuthState(): " + e.getMessage());
                        }
                    }
                }
            };

            return localFirebaseAuthStateListener;
        } catch (Exception e) {
            Log.e(TAG, "listenFirebaseAuthState()");
            return null;
        }
    }

    private void connectGoogleApiClient() {
        Log.w(TAG, "connectGoogleApiClient()");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppInvite.API)
                .build();

        mGoogleApiClient.connect();
    }

    private void getUserTrackedList() {
        Log.w(TAG, "getUserTrackedList()");
        try {
            dbRef_userTrackedList = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/userTrackedList/");
            userTrackedListListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        // ===>  receives the a new list of userTrackeds
                        Log.w(TAG, "getUserTrackedList().onDataChange(): " + dataSnapshot.getValue().toString());

                        // stores the size of the list
                        userTrackedListSize = (int) dataSnapshot.getChildrenCount();

                        // removes the marker from the map
                        removeUserMarkersFromMap();

                        // clears the markersOnMap
                        markersOnMap.clear();

                        // for each user tracked:
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            // if the userTracked has value TRUE
                            if ((boolean) dsp.getValue()) {
                                // gets information about that user
                                getUserInfo(dsp.getKey());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getUserTrackedList(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "getUserTrackedList(): The read failed: " + databaseError.getCode());
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "getUserTrackedList(): " + e.getMessage());
        }
    }

    private void removeUserMarkersFromMap() {
//        Log.w(TAG, "removeUserMarkersFromMap()");
        try {
            if (markersOnMap != null) {
                for (int i = 0; i < markersOnMap.size(); i++) {
                    markersOnMap.get(i).getMarker().remove();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "removeUserMarkersFromMap(): " + e.getMessage());
        }
    }

    private void getUserInfo(String user) {
//        Log.w(TAG, "getUserInfo(): " + user);

        try {
            DatabaseReference dbRef_user = database.getReference("/users/" + user + "/userInfo/");
            dbRef_user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // receives information about a user
//                    Log.i(TAG, "getUserInfo().onDataChange(): " + dataSnapshot.getValue());
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                    // if the information exists create a marker on the map for him, else, delete it from trackedList
                    if (userInfo != null) {

                        createMarkersOnMap(userInfo);

                    } else {
                        // TODO: if userInfo is null, the user should be deleted from userTracked list
                        Log.e(TAG, "getUserInfo: desc is NULL");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getUserInfo(): " + e.getMessage());
            Toast.makeText(this, "getUserInfo(): " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createMarkersOnMap(UserInfo userInfo) {
//        Log.w(TAG, "createMarkersOnMap() for: " + userInfo.getEmail());

        try {

            // creates a markerOnMapObject for the user
            MarkerOnMap markerOnMap = new MarkerOnMap(userInfo);

            // creates a marker on the map for the user (user picture)
            Marker userMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0d, 0d))
                    .title(markerOnMap.getUserInfo().getDisplayName())
                    .visible(false));

            // sets the userMarker on the markerOnMap object
            markerOnMap.setMarker(userMarker);

            // creates a marker on the map for the user info (update, battery, activity, etc...)
            Marker infoMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0d, 0d))
                    .title(markerOnMap.getUserInfo().getDisplayName())
                    .visible(false)
                    .anchor(0f, 0.3f));

            // sets the infoMarker on the markerOnMap object
            markerOnMap.setMarkerInfo(infoMarker);

            // loads user Picture
            loadUserPicture(markerOnMap);

        } catch (Exception e) {
            Log.e(TAG, "createMarkersOnMap(): " + e.getMessage());
        }
    }

    private void loadUserPicture(final MarkerOnMap markerOnMap) {
//        Log.w(TAG, "loadUserPicture(): " + markerOnMap.getUserInfo().getEmail());

        try {

            // loads the user picture from disk
            Bitmap userPicture = loadUserPictureFromDisk(markerOnMap.getUserInfo().getEmail().replace(".", "!"));

            // if user picture exists (is not null)
            if (userPicture != null) {
                // sets the user picture on the markerOnMap object
                markerOnMap.setUserPicture(userPicture);
                // sets the user gray picture on the markerOnMap object (when turned off)
                markerOnMap.setUserPictureGray(toGrayscale(userPicture));
                // sets the user picture to the userMarker stored in the markerOnMap object
                markerOnMap.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(userPicture));
                // sets userMarker visible in the markerOnMap object
                markerOnMap.getMarker().setVisible(true);

                // creates the locationListener for the user in the markerOnMap object
                createUserLocationListener(markerOnMap);

                // dismiss the progressBar if all pictures are loaded and all locations received
//                hideProgressBarOnMarkersOnMapReady();
            } else {
                // if the picture does not exist on disk, it is downloaded from Google account and then stored on disk
                AsyncMarkerOnMap obj = new AsyncMarkerOnMap(markerOnMap.getUserInfo().getIcon(), markerOnMap.getUserInfo().getEmail().replace(".", "!")) {
                    @Override
                    protected void onPostExecute(Bitmap bmp) {
                        super.onPostExecute(bmp);
                        if (bmp != null) {
                            markerOnMap.setUserPicture(bmp);
                            markerOnMap.setUserPictureGray(toGrayscale(bmp));
                            markerOnMap.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
                            markerOnMap.getMarker().setVisible(true);
                            createUserLocationListener(markerOnMap);
                        }

//                        hideProgressBarOnMarkersOnMapReady();
                    }
                };
                obj.execute();

            }
        } catch (Exception e) {
            Log.e(TAG, "loadUserPicture(): " + e.getMessage());
        }
    }

    private Bitmap loadUserPictureFromDisk(String user) {
//        Log.w(TAG, "loadUserPictureFromDisk()");
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/locator");
            File image = new File(folder, user);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "loadUserPictureFromDisk(): " + e.getMessage());
            return null;
        }
    }

//    private void hideProgressBarOnMarkersOnMapReady() {
//        Log.w(TAG, "hideProgressBarOnMarkersOnMapReady()");
//        showProgressBar(false, "", "hideProgressBarOnMarkersOnMapReady()");
//        eventMarkersOnMapReady.doEvent();
//    }


    public Bitmap getCroppedCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private void createUserLocationListener(final MarkerOnMap markerOnMap) {
//        Log.w(TAG, "createUserLocationListener(): " + markerOnMap.getUserInfo().getEmail());
        try {
            dbRef_userLocation = database.getReference("/users/" + markerOnMap.getUserInfo().email.replace(".", "!") + "/location/");
            ValueEventListener eventListener = dbRef_userLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                    if (userLocation != null) {
                        markerOnMap.setUserLocation(userLocation);
                        markerOnMap.getMarker().setPosition(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                        markerOnMap.getMarkerInfo().setIcon(BitmapDescriptorFactory.fromBitmap(generateMarkerInfo(markerOnMap)));
                        markerOnMap.getMarkerInfo().setPosition(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                        if (userTrackedListSize == markersOnMap.size() && zoomToUser && locationsValidated()) {
                            zoomToUser();
                        }
                    } else {
                        //Toast.makeText(MainActivity.this, "createUserLocationListener(): userLocation is NULL", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "createUserLocationListener(): The read failed: " + databaseError.getCode());
                }

            });

            markerOnMap.setLocationEventListener(eventListener);
            signInListenerForMarkerOnMap(markerOnMap);

        } catch (Exception e) {
            Log.e(TAG, "createUserLocationListener(): " + e.getMessage());
        }
    }

    private boolean locationsValidated() {
        try {
            for (int i = 0; i < markersOnMap.size(); i++) {
                if ((markersOnMap.get(i).getUserLocation().getLongitude() == 0.0d) ||
                        (markersOnMap.get(i).getUserLocation().getLatitude() == 0.0d))
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void zoomToUser() {
        Log.w(TAG, "zoomToUser()");

        hidePlacesAndInfo();

        if (userToZoom == null || userToZoom.equals(""))
            userToZoom = mFirebaseUser.getEmail().replace(".", "!");

        Log.i(TAG, "markersOnMap.size(): " + markersOnMap.size());
        Log.i(TAG, "userToZoom: " + userToZoom);

        for (int i = 0; i < markersOnMap.size(); i++) {
            Log.i(TAG, "markersOnMap.get(i).getUserInfo().getEmail(): " + markersOnMap.get(i).getUserInfo().getEmail());
            if (markersOnMap.get(i).getUserInfo().getEmail().replace(".", "!").equals(userToZoom)) {
                showProgressBar(false, "", "createUserLocationListener");
                Log.i(TAG, "user " + userToZoom + " found!");
                showProgressBar(false, "", "zoomToUser");
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(markersOnMap.get(i).getMarker().getPosition(), 16);
                mMap.animateCamera(cu);
                zoomToUser = false;
                userToZoom = "";
                break;
            }
        }
    }

    private void showAddUserTutorial() {
        if (!sharedPref.getBoolean(getResources().getString(R.string.show_add_user_tutorial), false)) {
            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    showRightPanel(false);
//                    showLeftPanel(false);
//                    showMarkerInfo = false;
//                    hidePlacesAndInfo();

//                    Point point = getMenuItemPosition(R.id.item_add_user);
//                    RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) relativeLayout_highlight_add_user.getLayoutParams();
//                    relativeParams.setMargins(point.x + Util.pxToDp(this, 4), Util.pxToDp(this, 4), 0, 0);  // left, top, right, bottom
//                    relativeLayout_highlight_add_user.setLayoutParams(relativeParams);
//
//                    Handler handler_show_bubble = new Handler();
//                    handler_show_bubble.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            relativeLayout_highlight_add_user.setVisibility(VISIBLE);
//
//                            final LinearLayout linearLayout_bubble = (LinearLayout) findViewById(R.id.linearLayout_bubble);
//                            BubbleDrawable myBubble = new BubbleDrawable(0);
//                            myBubble.setCornerRadius(20);
//                            Rect myViewRect = new Rect();
//                            imageView_circle_add_user.getGlobalVisibleRect(myViewRect);
//                            myBubble.setPointerAlignment(myViewRect.centerX());
//                            myBubble.setPadding(25, 25, 25, 25);
//                            linearLayout_bubble.setBackgroundDrawable(myBubble);
//                            TextView textView_bubble = (TextView) findViewById(R.id.textView_bubble);
//                            textView_bubble.setPadding(0, 40, 0, 0);
//                            textView_bubble.setText(getString(R.string.textView_bubble));
//                            Handler handler_hide_bubble = new Handler();
//                            handler_hide_bubble.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    relativeLayout_highlight_add_user.setVisibility(GONE);
//                                    linearLayout_bubble.setVisibility(GONE);
//
////                                    showRightPanel(true);
////                                    showLeftPanel(true);
////                                    showMarkerInfo = true;
////                                    showPlacesAndInfo();
//                                }
//                            }, 6000);
//                        }
//                    }, 100);

                }
            }, 3000);
        }
    }

//    private void showRightPanel(boolean b) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            if (b) {
//                linearLayout_rightPanel.setVisibility(VISIBLE);
//            } else {
//                linearLayout_rightPanel.setVisibility(GONE);
//            }
//        }
//    }

//    private void showLeftPanel(boolean b) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            if (b) {
//                linearLayout_leftPanel.setVisibility(VISIBLE);
//            } else {
//                linearLayout_leftPanel.setVisibility(GONE);
//            }
//        }
//    }

    public Bitmap getActivity(String activity) {
        try {
            switch (activity) {
                case "In Vehicle": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_car);
                }
                case "On Bicycle": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bike);
                }
                case "On Foot": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_walk);
                }
                case "Running": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_run);
                }
                case "Still": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_still);
                }
                case "Tilting": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps);
                }
                case "Walking": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_walk);
                }
                case "Unknown": {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps);
                }
                default: {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps);
                }
            }
        } catch (Exception e) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_gps);
        }
    }

    public Bitmap getBatteryLevelIcon(float batLevel) {
        batLevel *= 100;
//        Log.i(TAG, "getBatteryLevelIcon: " + batLevel);
        if (batLevel <= 5) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_5);
        } else if (5 < batLevel && batLevel < 15) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_10);
        } else if (15 <= batLevel && batLevel < 25) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_20);
        } else if (25 <= batLevel && batLevel < 35) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_30);
        } else if (35 <= batLevel && batLevel < 45) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_40);
        } else if (45 <= batLevel && batLevel < 55) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_50);
        } else if (55 <= batLevel && batLevel < 65) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_60);
        } else if (65 <= batLevel && batLevel < 75) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_70);
        } else if (75 <= batLevel && batLevel < 85) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_80);
        } else if (85 <= batLevel && batLevel < 95) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_bat_90);
        } else if (95 <= batLevel) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_battery_full);
        } else {
            return null;
        }
    }

    private Bitmap generateMarkerInfo(MarkerOnMap markerOnMap) {

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.density;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;

        Bitmap bmp = Bitmap.createBitmap(Util.pxToDp(this, 160), Util.pxToDp(this, 80), conf);
        Canvas canvas = new Canvas(bmp);

        float leftx = Util.pxToDp(this, 20);
        float topy = Util.pxToDp(this, 1);
        float rightx = Util.pxToDp(this, 159);
        float bottomy = Util.pxToDp(this, 79);
        // FILL
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.light_gray));
        paint.setStyle(Paint.Style.FILL);
        RectF rect = new RectF(leftx, topy, rightx, bottomy);
        canvas.drawRoundRect(rect, Util.pxToDp(this, 4), Util.pxToDp(this, 4), paint);

        paint.setStrokeWidth(Util.pxToDp(this, 1));
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        // BORDER
        canvas.drawRoundRect(rect, Util.pxToDp(this, 4), Util.pxToDp(this, 4), paint);

        // NAME
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(Util.pxToDp(this, 12));
        canvas.drawText(markerOnMap.getUserInfo().getDisplayName(), Util.pxToDp(this, 25), Util.pxToDp(this, 15), paint);

        // CLOCK
        Bitmap clock_raw = BitmapFactory.decodeResource(getResources(), R.drawable.ic_clock);
        Bitmap clock = Bitmap.createScaledBitmap(clock_raw, Util.pxToDp(this, 15), Util.pxToDp(this, 15), false);
        canvas.drawBitmap(clock, Util.pxToDp(this, 25), Util.pxToDp(this, 20), paint);

        // BATTERY
        Bitmap battery_raw = getBatteryLevelIcon(markerOnMap.getUserLocation().getBattery());
        Bitmap battery = Bitmap.createScaledBitmap(battery_raw, Util.pxToDp(this, 17), Util.pxToDp(this, 16), false);
        canvas.drawBitmap(battery, Util.pxToDp(this, 24), Util.pxToDp(this, 38), paint);

        // ACTIVITY
        Bitmap activity_raw = getActivity(markerOnMap.getUserLocation().getState());
        if (markerOnMap.getUserLocation().getState() == null || markerOnMap.getUserLocation().getState().equals("Still")) {
            Bitmap activity = Bitmap.createScaledBitmap(activity_raw, Util.pxToDp(this, 23), Util.pxToDp(this, 17), false);
            canvas.drawBitmap(activity, Util.pxToDp(this, 22), Util.pxToDp(this, 56), paint);
        } else {
            Bitmap activity = Bitmap.createScaledBitmap(activity_raw, Util.pxToDp(this, 15), Util.pxToDp(this, 15), false);
            canvas.drawBitmap(activity, Util.pxToDp(this, 25), Util.pxToDp(this, 60), paint);
        }

        // POWER
        Bitmap power_raw = BitmapFactory.decodeResource(getResources(), R.drawable.ic_power_settings_new_black_24dp);
        Bitmap power = Bitmap.createScaledBitmap(power_raw, Util.pxToDp(this, 17), Util.pxToDp(this, 17), false);
        canvas.drawBitmap(power, Util.pxToDp(this, 90), Util.pxToDp(this, 38), paint);

        // PRECISION
        Bitmap precision_raw = BitmapFactory.decodeResource(getResources(), R.drawable.ic_precision);
        Bitmap precision = Bitmap.createScaledBitmap(precision_raw, Util.pxToDp(this, 17), Util.pxToDp(this, 17), false);
        canvas.drawBitmap(precision, Util.pxToDp(this, 90), Util.pxToDp(this, 58), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(Util.pxToDp(this, 13));
        paint.setAlpha(95);
        long lastUpdate = 0;
        try {
            lastUpdate = Long.valueOf(markerOnMap.getUserLocation().getLastUpdate());
            canvas.drawText(calculateTimePassed(lastUpdate), Util.pxToDp(this, 45), Util.pxToDp(this, 32), paint);
        } catch (Exception e) {
            canvas.drawText(getString(R.string.interrogation_mark), Util.pxToDp(this, 45), Util.pxToDp(this, 32), paint);
        }
        canvas.drawText(String.valueOf(Math.round(markerOnMap.getUserLocation().getBattery() * 100)) + "%", Util.pxToDp(this, 45), Util.pxToDp(this, 52), paint);
        String confidence = new String(markerOnMap.getUserLocation().getConfidence());
        if (confidence.equals(""))
            confidence = "--";
        canvas.drawText(confidence + "%", Util.pxToDp(this, 45), Util.pxToDp(this, 72), paint);
        if (markerOnMap.isPowerOn()) {
            canvas.drawText(getString(R.string.on), Util.pxToDp(this, 113), Util.pxToDp(this, 52), paint);
        } else {
            canvas.drawText(getString(R.string.off), Util.pxToDp(this, 113), Util.pxToDp(this, 52), paint);
        }
        canvas.drawText(String.valueOf(Math.round(markerOnMap.getUserLocation().getAccuracy())) + "m", Util.pxToDp(this, 113), Util.pxToDp(this, 72), paint);

//
//
//        Bitmap activity = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_cycling);
////        Bitmap bmpFinal = Bitmap.createScaledBitmap(battery, 20, 20, false);
//
//        Paint color = new Paint();
//        color.setTextSize(35);
//        color.setStrokeWidth(0.5f);
//        color.setColor(getResources().getColor(R.color.colorPrimaryLight));
//
//        Paint textColor = new Paint();
//        textColor.setTextSize(35);
//        textColor.setStrokeWidth(0.5f);
//        textColor.setColor(getResources().getColor(R.color.black));
//
//        RectF r = new RectF(100,0,400,200);
//        canvas1.drawRoundRect(r, 2f, 2f, color);
//
//        Bitmap text = drawTextToBitmap(this, "battery");
//
//        canvas1.drawBitmap(battery, 100, 0, color);
//        canvas1.drawBitmap(text, 2000, 0, textColor);
//        canvas1.drawBitmap(activity, 100, 100, color);

        return bmp;
    }

    public String calculateTimePassed(long timeStart) {
        Calendar now = Calendar.getInstance();

        long milliseconds1 = timeStart;
        long milliseconds2 = now.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;

//        Log.i(TAG, "Now: " + milliseconds2);
//        Log.i(TAG, "time: " + timeStart);
//        Log.i(TAG, "Diff: " + diff);
//        Log.i(TAG, "Minutes: " + TimeUnit.MILLISECONDS.toMinutes(diff));
//        Log.i(TAG, "hours: " + TimeUnit.MILLISECONDS.toHours(diff));
//        Log.i(TAG, "diffHours: " + TimeUnit.MILLISECONDS.toDays(diff));


        if (TimeUnit.MILLISECONDS.toSeconds(diff) < 60)
            return getString(R.string.now);

        if (TimeUnit.MILLISECONDS.toMinutes(diff) < 60)
            return getString(R.string.minutes_ago, TimeUnit.MILLISECONDS.toMinutes(diff));

        if (TimeUnit.MILLISECONDS.toHours(diff) < 24)
            return getString(R.string.hours_ago, TimeUnit.MILLISECONDS.toHours(diff));

        if (TimeUnit.MILLISECONDS.toDays(diff) > 1)
            return getString(R.string.days_ago, TimeUnit.MILLISECONDS.toDays(diff));

        return getString(R.string.undefined);
    }


    private void signInListenerForMarkerOnMap(final MarkerOnMap markerOnMap) {
//        Log.w(TAG, "signInListenerForMarkerOnMap(): " + markerOnMap.getUserInfo().getEmail());

        try {
            dbRef_signIn = database.getReference("/users/" + markerOnMap.getUserInfo().email.replace(".", "!") + "/signIn/");
            signInValueEventListener = dbRef_signIn.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    signIn = (boolean) dataSnapshot.getValue();
                    updatePictureOnMarkerOnMap(markerOnMap);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "signInListenerForMarkerOnMap(): The read failed: " + databaseError.getCode());
                }
            });

            markerOnMap.setSignInEventListener(signInValueEventListener);

            powerOnListenerForMarkerOnMap(markerOnMap);

        } catch (Exception e) {
            Log.e(TAG, "signInListenerForMarkerOnMap(): " + e.getMessage());
        }
    }

    private void listenPlacesChanges() {
        Log.w(TAG, "listenPlacesChanges()");
        try {
            dbRef_places = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/places/");
            placesValueEventListener = dbRef_places.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "listenPlacesChanges().onDataChange(): " + dataSnapshot.getValue());
                        for (int i = 0; i < geoPlaces.size(); i++) {
                            try {
                                geoPlaces.get(i).getCircle().remove();
                                geoPlaces.get(i).getMarker().remove();
                            } catch (Exception e) {
                                // if there are places not shown by users choise
                            }
                        }

                        geoPlaces.clear();

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            GeoPlace geoPlace = new GeoPlace();
                            String mask = "+" + mFirebaseUser.getEmail().replace(".", "!");
                            geoPlace.setName(dsp.getKey().replace(mask, ""));
                            geoPlace.setFence(dsp.getValue(Fence.class));

                            geoPlaces.add(geoPlace);
                        }

                        drawGeoPlacesOnMap();

                    } catch (Exception e) {
                        for (int i = 0; i < geoPlaces.size(); i++) {
                            geoPlaces.get(i).getCircle().remove();
                            geoPlaces.get(i).getMarker().remove();
                        }
                        geoPlaces.clear();
                        Log.e(TAG, "listenPlacesChanges(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "listenPlacesChanges(): The read failed: " + databaseError.getCode());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "listenPlacesChanges(): " + e.getMessage());
        }
    }


    private void drawGeoPlacesOnMap() {
        Log.w(TAG, "drawGeoPlacesOnMap()");
        for (int i = 0; i < geoPlaces.size(); i++) {
            if (geoPlaces.get(i).getFence().isShowOnMap()) {

                LatLng latLng = new LatLng(geoPlaces.get(i).getFence().getLat(), geoPlaces.get(i).getFence().getLng());

                CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .radius(geoPlaces.get(i).getFence().getRadius())
                        .strokeColor(0x772E2E2E)
                        .strokeWidth(1.8f)
                        .fillColor(0x77AAAAAA);

                Circle circle = mMap.addCircle(circleOptions);

                geoPlaces.get(i).setCircle(circle);
                geoPlaces.get(i).setCircleOptions(circleOptions);
                String mask = "+" + geoPlaces.get(i).getFence().getOwner().replace(".", "!");
                String placeName = geoPlaces.get(i).getName().replace(mask, "");
                MarkerOptions options = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(drawTextToBitmap(this, placeName.toUpperCase())));
                Marker newMarker = mMap.addMarker(options);

                geoPlaces.get(i).setMarker(newMarker);

            }
        }
    }

    private void hidePlacesAndInfo() {
        Log.w(TAG, "hidePlacesAndInfo()");

        for (int i = 0; i < geoPlaces.size(); i++) {
            try {
                geoPlaces.get(i).getMarker().setVisible(false);
                geoPlaces.get(i).getCircle().setVisible(false);
            } catch (Exception e) {
                Log.e(TAG, "hidePlacesAndInfo(): loop on geoPlaces: " + e.getMessage());
            }
        }

        for (int i = 0; i < markersOnMap.size(); i++) {
            try {
                markersOnMap.get(i).getMarkerInfo().setVisible(false);
            } catch (Exception e) {
                Log.e(TAG, "hidePlacesAndInfo(): loop on markersOnMap: " + e.getMessage());
            }
        }

    }

    private void showPlacesAndInfo() {
        Log.w(TAG, "showPlacesAndInfo()");
        if (showMarkerInfo) {

            for (int i = 0; i < geoPlaces.size(); i++) {
                try {
                    geoPlaces.get(i).getMarker().setVisible(true);
                    geoPlaces.get(i).getCircle().setVisible(true);
                } catch (Exception e) {
                    Log.e(TAG, "showPlacesAndInfo(): loop on geoPlaces: " + e.getMessage());
                }
            }

            for (int i = 0; i < markersOnMap.size(); i++) {
                try {
                    markersOnMap.get(i).getMarkerInfo().setVisible(true);
                } catch (Exception e) {
                    Log.e(TAG, "showPlacesAndInfo(): loop on markersOnMap: " + e.getMessage());
                }
            }
        }
    }


    public Bitmap drawTextToBitmap(Context gContext,
                                   String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(700, 60, conf); // this creates a MUTABLE bitmap


        Bitmap.Config bitmapConfig =
                bmp.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bmp = bmp.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bmp);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(240, 20, 20));
        // text size in pixels
        paint.setTextSize((int) (10 * scale));
        // text shadow
        paint.setShadowLayer(2f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bmp.getWidth() - bounds.width()) / 2;
        int y = (bmp.getHeight() + bounds.height()) / 2;

        canvas.drawText(gText, x, y, paint);

        return bmp;
    }

    private void powerOnListenerForMarkerOnMap(final MarkerOnMap markerOnMap) {
        Log.w(TAG, "powerOnListenerForMarkerOnMap(): " + markerOnMap.getUserInfo().getEmail());

        try {
            dbRef_powerOn = database.getReference("/users/" + markerOnMap.getUserInfo().email.replace(".", "!") + "/powerOn/");
            ValueEventListener powerOnEventListener = dbRef_powerOn.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "powerOnListenerForMarkerOnMap().markerOnMap: " + markerOnMap.getUserInfo().getEmail());
                    Log.i(TAG, "powerOnListenerForMarkerOnMap().onDataChange(): " + dataSnapshot.getValue());
                    if (dataSnapshot.getValue() != null) {
                        markerOnMap.setPowerOn((boolean) dataSnapshot.getValue());
                        updatePictureOnMarkerOnMap(markerOnMap);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "powerOnListenerForMarkerOnMap(): The read failed: " + databaseError.getCode());
                }
            });
            markerOnMap.setPowerOnEventListener(powerOnEventListener);
            markersOnMap.add(markerOnMap);

        } catch (Exception e) {
            Log.e(TAG, "powerOnListenerForMarkerOnMap(): " + e.getMessage());
        }
    }

    private void updatePictureOnMarkerOnMap(MarkerOnMap markerOnMap) {
        Log.i(TAG, "updatePictureOnMarkerOnMap for: " + markerOnMap.getUserInfo().getEmail());
        try {
            for (int i = 0; i < markersOnMap.size(); i++) {
                if (markerOnMap.getUserInfo().getEmail().equals(markersOnMap.get(i).getUserInfo().getEmail())) {
                    if (signIn) {
                        if (markerOnMap.isPowerOn()) {
                            markersOnMap.get(i).getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(markerOnMap.getUserPicture()));
                            markersOnMap.get(i).getMarkerInfo().setIcon(BitmapDescriptorFactory.fromBitmap(generateMarkerInfo(markersOnMap.get(i))));
                        } else {
                            markersOnMap.get(i).getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(markerOnMap.getUserPictureGray()));
                            markersOnMap.get(i).getMarkerInfo().setIcon(BitmapDescriptorFactory.fromBitmap(generateMarkerInfo(markersOnMap.get(i))));
                        }
                    } else {
                        markersOnMap.get(i).getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(markerOnMap.getUserPictureGray()));
                        markersOnMap.get(i).getMarkerInfo().setIcon(BitmapDescriptorFactory.fromBitmap(generateMarkerInfo(markersOnMap.get(i))));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "updatePictureOnMarkerOnMap: " + e.getMessage());
        }
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        try {
            int width, height;
            height = bmpOriginal.getHeight();
            width = bmpOriginal.getWidth();

            Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmpGrayscale);
            Paint paint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bmpOriginal, 0, 0, paint);
            return bmpGrayscale;
        } catch (Exception e) {
            Log.e(TAG, "toGrayscale: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.w(TAG, "onMapReady");
        mMap = googleMap;

        setupMapMoveListener();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {

                } catch (Exception e) {
                    Log.e(TAG, "setOnMarkerClickListener(): " + e.getMessage());
                }
                return false;
            }
        });
    }

    private void setupMapMoveListener() {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (mMap.getCameraPosition().zoom < 16) {
                    hidePlacesAndInfo();
                } else {
                    showPlacesAndInfo();
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed");
    }

    protected void signOut() {
        try {
            Log.i(TAG, "signOut");
            showSignOutDialog();
        } catch (Exception e) {
            Log.e(TAG, "SignOut: " + e.getLocalizedMessage());
        }

    }

    private void showSignOutDialog() {

        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog);

        TextView textView_track_back_title = (TextView) dialog.findViewById(R.id.textView_dialog_editText_title);
        TextView textView_track_back_desc = (TextView) dialog.findViewById(R.id.textView_dialog_editText_text);
        TextView btn_track_back_done = (TextView) dialog.findViewById(R.id.textView_dialog_editText_ok);
        TextView btn_track_back_cancel = (TextView) dialog.findViewById(R.id.textView_dialog_editText_cancel);

        textView_track_back_title.setText(R.string.sign_out);
        textView_track_back_desc.setText(getResources().getString(R.string.logout_message));

        btn_track_back_done.setText(R.string.ok);
        btn_track_back_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApplication.signOut();
                dialog.dismiss();
                revokeAccess();
            }
        });

        btn_track_back_cancel.setText(R.string.cancel);
        btn_track_back_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.w(TAG, "onRestart()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w(TAG, "onStart()");
        try {

            showProgressBar(true, getString(R.string.loading_your_data), "onStart");

            checkAppUpdates();

            checkPremiumUser();

            zoomToUser = true;

            mAuth.addAuthStateListener(mAuthListener);
            if (userTrackedListListener != null) {
                userTrackedListSize = 0;
                dbRef_userTrackedList.addValueEventListener(userTrackedListListener);
            }
            if (placesValueEventListener != null) {
                dbRef_places.addValueEventListener(placesValueEventListener);
            }
            for (int i = 0; i < markersOnMap.size(); i++) {
                try {
                    if (markersOnMap.get(i).getLocationEventListener() != null) {
                        dbRef_userLocation.addValueEventListener(markersOnMap.get(i).getLocationEventListener());
                        dbRef_signIn.addValueEventListener(markersOnMap.get(i).getSignInEventListener());
                        dbRef_powerOn.addValueEventListener(markersOnMap.get(i).getPowerOnEventListener());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onStart(): loop markersOnMap: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onStart: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.w(TAG, "onNewIntent()");
        super.onNewIntent(intent);


        Bundle bundle = new Bundle();
        bundle = intent.getExtras();

        handleBundle(bundle);
    }

    private void handleBundle(Bundle bundle) {
        userToZoom = bundle.getString("user");

        try {
            restartedActivity = bundle.getBoolean("restartedActivity");
        } catch (Exception e) {
            Log.e(TAG, "onNewIntent(): restartedActivity");
        }

        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(bundle.getInt("notificationId"));
            Log.i(TAG, "onNewIntent(): Dismissing notification...");
        } catch (Exception e) {
            Log.e(TAG, "onNewIntent(): error dismissing notification: " + e.getMessage());
        }

        try {
            bundle.clear();
        } catch (Exception e) {
            Log.e(TAG, "onNewIntent(): bundle.clear(): " + e.getMessage());
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "onStop()");
        try {
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
            if (placesValueEventListener != null) {
                dbRef_places.removeEventListener(placesValueEventListener);
            }
            if (userTrackedListListener != null) {
                dbRef_userTrackedList.removeEventListener(userTrackedListListener);
            }
            for (int i = 0; i < markersOnMap.size(); i++) {
                if (markersOnMap.get(i).getLocationEventListener() != null) {
                    dbRef_userLocation.removeEventListener(markersOnMap.get(i).getLocationEventListener());
                    dbRef_signIn.removeEventListener(markersOnMap.get(i).getSignInEventListener());
                    dbRef_powerOn.removeEventListener(markersOnMap.get(i).getPowerOnEventListener());
                }
            }
            if (broadcastReceiverShutdown.isOrderedBroadcast()) {
                unregisterReceiver(broadcastReceiverShutdown);
            }
            if ((handler_restartActivity != null) && (runnable_restartActivity != null)) {
                handler_restartActivity.removeCallbacks(runnable_restartActivity);
            }

            hidePlacesAndInfo();
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getMessage());
        }
    }


    public class AsyncMarkerOnMap extends AsyncTask<Void, Void, Bitmap> {

        private String src, user;

        public AsyncMarkerOnMap(String src, String user) {
            Log.w(TAG, "AsyncMarkerOnMap()");
            this.src = src;
            this.user = user;
            Log.i(TAG, "AsyncMarkerOnMap.src: " + src);
            Log.i(TAG, "AsyncMarkerOnMap.user: " + user);
        }


        @Override
        protected Bitmap doInBackground(Void... params) {
            Log.i(TAG, "AsyncMarkerOnMap().doInBackground()");
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                Bitmap user_picture_raw = BitmapFactory.decodeStream(input);

                Bitmap aux = Bitmap.createScaledBitmap(
                        user_picture_raw,
                        Util.pxToDp(getApplicationContext(), 59),
                        Util.pxToDp(getApplicationContext(), 59),
                        false);
                user_picture_raw = getCroppedCircularBitmap(aux);

                Bitmap user_marker = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_marker);
                Bitmap aux2 = Bitmap.createScaledBitmap(user_marker, Util.pxToDp(getApplicationContext(), 100), Util.pxToDp(getApplicationContext(), 100), false);

                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(Util.pxToDp(getApplicationContext(), 100), Util.pxToDp(getApplicationContext(), 100), conf);
                Canvas canvas1 = new Canvas(bmp);

                Paint color = new Paint();
                color.setStyle(Paint.Style.FILL);
                color.setColor(getResources().getColor(R.color.colorPrimary));

                canvas1.drawBitmap(user_marker, Util.pxToDp(getApplicationContext(), 0), Util.pxToDp(getApplicationContext(), 0), color);
                canvas1.drawBitmap(user_picture_raw, Util.pxToDp(getApplicationContext(), 21), Util.pxToDp(getApplicationContext(), 9), color);

                saveUserPictureToDisk(bmp, user);

                return bmp;

//                return Bitmap.createScaledBitmap(bmp, Util.pxToDp(this, 70), Util.pxToDp(this, 70), false);
            } catch (IOException e) {
                Log.e(TAG, "AsyncMarkerOnMap().doInBackground(): " + e.getMessage());
                return null;
            }
        }
    }

    private void saveUserPictureToDisk(Bitmap bitmap, String user) {
        Log.w(TAG, "saveUserPictureToDisk()");
        FileOutputStream out = null;
        File folder = new File(Environment.getExternalStorageDirectory() + "/locator");

        try {
            if (!folder.exists()) {
                folder.mkdir();
            }
            out = new FileOutputStream(folder + "/" + user);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "saveUserPictureToDisk(): " + e.getMessage());
            }
        }
    }

    public class AsyncCircularPicture extends AsyncTask<Void, Void, Bitmap> {

        private String src;

        public AsyncCircularPicture(String src) {
            this.src = src;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                myBitmap = getCroppedCircularBitmap(myBitmap);
                myBitmap = Bitmap.createScaledBitmap(myBitmap, Util.pxToDp(MainActivity.this, 160), Util.pxToDp(MainActivity.this, 160), false);

                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private Point getMenuItemPosition(int id) {
        View menuView = findViewById(id);
        int[] location = new int[2];
        menuView.getLocationOnScreen(location);
        int locationX = location[0];
        int locationY = location[1];

        return new Point(locationX, locationY);
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menuItem_search = menu.findItem(R.id.item_search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "searchView.onQueryTextChange()");
                listView_search.setVisibility(VISIBLE);
                final ArrayList<MarkerOnMap> array = new ArrayList<MarkerOnMap>();
                ArrayList<String> names = new ArrayList<String>();


                for (int i = 0; i < markersOnMap.size(); i++) {
                    if (markersOnMap.get(i).getUserInfo().getDisplayName().toLowerCase().contains(newText.toLowerCase())) {
                        array.add(markersOnMap.get(i));
                        names.add(markersOnMap.get(i).getUserInfo().getDisplayName());
                    }
                }

                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, names);
                listView_search.setAdapter(adapter);
                listView_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        searchView.setIconified(true);
                        searchView.onActionViewCollapsed();
                        menuItem_search.collapseActionView();
                        listView_search.setVisibility(GONE);
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(array.get(i).getMarker().getPosition(), 16);
                        mMap.animateCamera(cu);                  // <<------ zoom in the first userTracked update
                    }
                });

                if (array.size() == 1) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(array.get(0).getMarker().getPosition(), 16);
                    mMap.animateCamera(cu);                  // <<------ zoom in the first userTracked update
                }
                return false;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView_search.setVisibility(GONE);
                    }
                }, 100);
                return false;
            }
        });


        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "searchView.setOnSearchClickListener()");
                listView_search.setVisibility(VISIBLE);

                ArrayList<String> names = new ArrayList<String>();


                for (int i = 0; i < markersOnMap.size(); i++) {
                    names.add(markersOnMap.get(i).getUserInfo().getDisplayName());
                    Log.i(TAG, "Name: " + names.get(i));
                }

                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, names);
                listView_search.setAdapter(adapter);
                listView_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        searchView.setIconified(true);
                        searchView.onActionViewCollapsed();
                        menuItem_search.collapseActionView();
                        listView_search.setVisibility(GONE);
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(markersOnMap.get(i).getMarker().getPosition(), 16);
                        mMap.animateCamera(cu);                  // <<------ zoom in the first userTracked update
                    }
                });

            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {

            if (actionBarDrawerToggle.onOptionsItemSelected(item))
                return true;

            switch (item.getItemId()) {

//                case R.id.item_add_user:
//                    startInviteActivity();
//                    break;
//
//                case R.id.item_add_place:
//                    addPlace();
//                    break;

            }
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            Log.e(TAG, "onStop: " + e.getLocalizedMessage());
            return false;
        }
    }

    private void startInviteActivity() {
        startActivity(new Intent(this, InviteActivity.class));
    }

    private void inviteUser() {
        startActivity(new Intent(this, InvitationSentActivity.class));
    }


    private void checkPlacesQty() {
        Log.w(TAG, "checkPlacesQty()");
        try {
            DatabaseReference dbRef_premium = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/places/");
            dbRef_premium.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "checkPlacesQty().onDataChange(): " + dataSnapshot.getValue());
                        if (dataSnapshot.getChildrenCount() >= 1) {
                            Intent intent = new Intent(MainActivity.this, InAppBillingActivity.class);
                            startActivity(intent);
                        } else {
                            createFence();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "checkPlacesQty: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "checkPlacesQty(): The read failed: " + databaseError.getCode());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "checkPlacesQty(): " + e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        try {
            if (requestCode == REQUEST_INVITE) {
                if (resultCode == RESULT_OK) {
//                     Get the invitation IDs of all sent messages
                    String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                    for (String id : ids) {
                        createInviteOnFirebase(id);
                    }
                } else {
                    Log.d(TAG, "failed: " + resultCode);
                }
            }
            if (requestCode == PLUS_ONE_REQUEST_CODE) {
                if (resultCode == -1) {
                    Toast.makeText(this, getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("recommended", true);
                            editor.commit();
                            mPlusOneButton.setVisibility(GONE);
                        }
                    }, 3000);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: " + e.getMessage());
        }
    }

    private void createInviteOnFirebase(final String id) {
        Log.w(TAG, "createInviteOnFirebase()");
        try {
            dbRef_createInvite = database.getReference("/invites/" + id);
            dbRef_createInvite.setValue(mFirebaseUser.getEmail().replace(".", "!"), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "createInviteOnFirebase NOT executed:" + databaseError.getMessage());
                        Toast.makeText(MainActivity.this, "createInviteOnFirebase NOT executed:", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "createInviteOnFirebase executed successfully: " + id);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "createInviteOnFirebase: " + e.getMessage());
        }
    }

    private void checkInvites() {
        Log.w(TAG, "checkInvites()" + id);
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);

                                    getInviter(invitationId);
                                }
                            }
                        });
    }

    private void getInviter(final String invitationId) {
        Log.w(TAG, "getInviter()");
        try {
            DatabaseReference dbRef_getInvite = database.getReference("/invites/" + invitationId);
            dbRef_getInvite.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Invite invite = dataSnapshot.getValue(Invite.class);
                        if (invite != null) {
                            Log.i(TAG, "getInviter().onDataChange(): " + invite.getUser());
                            addInviterToMyTrackedList(invite.getUser());
                            addMeToInvitersTrackedList(invite.getUser());

                            addInviterPlacesInMyPlaces(invite.getUser());
                            addMyPlacesInInviterPlaces(invite.getUser());

                            deleteInvitation(invitationId);
                        } else {
                            Log.i(TAG, "getInviter().onDataChange(): NULL");
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "Handling old invitations");
                        // Handling old invitations
                        String user = (String) dataSnapshot.getValue();
                        if (user != null) {
                            Log.i(TAG, "getInviter().onDataChange(): " + user);
                            addInviterToMyTrackedList(user);
                            addMeToInvitersTrackedList(user);

                            addInviterPlacesInMyPlaces(user);
                            addMyPlacesInInviterPlaces(user);

                            deleteInvitation(invitationId);
                        } else {
                            Log.i(TAG, "getInviter().onDataChange() - Handling old invitations : NULL");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "getInviter: " + e.getMessage());
        }

    }

    private void addInviterPlacesInMyPlaces(final String inviter) {
        Log.w(TAG, "addInviterPlacesInMyPlaces()");
        try {
            DatabaseReference dbRef = database.getReference("/users/" + inviter.replace(".", "!") + "/places/");
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.i(TAG, "addInviterPlacesInMyPlaces().onDataChange(): " + dataSnapshot.getValue().toString());
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            GeoPlace geoPlace = new GeoPlace();
                            geoPlace.setName(dsp.getKey());
                            geoPlace.setFence(dsp.getValue(Fence.class));

                            if (geoPlace.getFence().getOwner().equals(inviter.replace(".", "!"))) {
                                createPlace(mFirebaseUser.getEmail().replace(".", "!"), geoPlace.getName(), geoPlace.getFence());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "addInviterPlacesInMyPlaces" + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "addInviterPlacesInMyPlaces(): The read failed: " + databaseError.getCode());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "addInviterPlacesInMyPlaces(): " + e.getMessage());
        }
    }


    private void addMyPlacesInInviterPlaces(final String inviter) {
        Log.w(TAG, "addInviterPlacesInMyPlaces()");
        try {
            DatabaseReference dbRef = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/places/");
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.i(TAG, "addInviterPlacesInMyPlaces().onDataChange(): " + dataSnapshot.getValue().toString());
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            GeoPlace geoPlace = new GeoPlace();
                            geoPlace.setName(dsp.getKey());
                            geoPlace.setFence(dsp.getValue(Fence.class));

                            if (geoPlace.getFence().getOwner().equals(mFirebaseUser.getEmail().replace(".", "!"))) {
                                createPlace(inviter, geoPlace.getName(), geoPlace.getFence());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "addInviterPlacesInMyPlaces" + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "addInviterPlacesInMyPlaces(): The read failed: " + databaseError.getCode());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "addInviterPlacesInMyPlaces(): " + e.getMessage());
        }
    }

    private void addMeToInvitersTrackedList(String inviter) {
        Log.w(TAG, "addMeToInvitersTrackedList()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + inviter + "/userTrackedList/" + mFirebaseUser.getEmail().replace(".", "!"));
            ref.setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "addMeToInvitersTrackedList could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "addMeToInvitersTrackedList created successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "addMeToInvitersTrackedList: " + e.getMessage());
        }

    }

    private void deleteInvitation(String invitationId) {
        Log.w(TAG, "deleteInvitation()");
        try {
            DatabaseReference dbRef_getInvite = database.getReference("/invites/" + invitationId);
            dbRef_getInvite.setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "deleteInvitation could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "deleteInvitation executed successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deleteInvitation: " + e.getMessage());
        }
    }

    private void addInviterToMyTrackedList(String user) {
        Log.w(TAG, "addInviterToMyTrackedList()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/userTrackedList/" + user);
            ref.setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "addInviterToMyTrackedList could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "addInviterToMyTrackedList created successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "addInviterToMyTrackedList: " + e.getMessage());
        }
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onBackPressed() {
        if (listView_search.getVisibility() == VISIBLE) {
            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
            menuItem_search.collapseActionView();
            listView_search.setVisibility(GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void setupAds(boolean show) {
        Log.w(TAG, "setupAds()");
        try {

            adView = (AdView) findViewById(R.id.adView);
            adView.setVisibility(View.GONE);
            linearLayout_adView.setVisibility(GONE);

            if (show) {
                adView.setEnabled(true);
                adRequest = new AdRequest.Builder()
                        .addTestDevice("C6E27E792E9C776653A67DDF90F3CB03")
                        .build();
            } else {
                adView.setEnabled(false);
                adView.setVisibility(GONE);
                linearLayout_adView.setVisibility(GONE);
            }


            adView.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout_adView.setVisibility(VISIBLE);
                            adView.setVisibility(View.VISIBLE);
                        }
                    }, 3000);
                }

                public void onAdFailedToLoad(int errorcode) {
                    linearLayout_adView.setVisibility(GONE);
                    adView.setVisibility(GONE);

                }

                public void onAdOpened() {
                }

                public void onAdClosed() {
                    linearLayout_adView.setVisibility(GONE);
                    adView.setVisibility(View.GONE);
                    adView.loadAd(adRequest);
                }

                public void onAdLeftApplication() {
                    linearLayout_adView.setVisibility(GONE);
                    adView.setVisibility(View.GONE);
                }
            });
            adView.loadAd(adRequest);

        } catch (Exception e) {
            Log.e(TAG, "Exception - adView: " + e.getLocalizedMessage());
        } finally {
            adView.setVisibility(View.GONE);
        }

    }

    private void showProgressBar(boolean show, String text, String from) {
        Log.w(TAG, "showProgressBar(): " + show + " from " + from);
        if (show) {
            linearLayout_progress_bar_main.setVisibility(VISIBLE);
            textView_progressbar.setText(text);

            handler_restartActivity = new Handler();
            runnable_restartActivity = new Runnable() {

                @Override
                public void run() {
                    if (linearLayout_progress_bar_main.getVisibility() == VISIBLE && !restartedActivity) {
                        Log.i(TAG, "showProgressBar().run(): restartActivity");
                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                        Bundle b = new Bundle();
                        b.putBoolean("restartedActivity", true);
                        i.putExtras(b);
                        finish();
                        startActivity(i);
                    }

                    if (linearLayout_progress_bar_main.getVisibility() == VISIBLE && restartedActivity) {
                        Log.i(TAG, "showProgressBar().run(): finishActivity");
                        Snackbar.make(linearLayout_progress_bar_main, R.string.no_network_try_again_later, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                                restartedActivity = false;
                                Handler handler1 = new Handler();
                                handler1.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            linearLayout_progress_bar_main.setVisibility(View.GONE);
                                        } catch (Exception e) {
                                            Log.e(TAG, "showProgressBar(): handler1.postDelayed(): " + e.getMessage());
                                        }
                                    }
                                }, 400);
                            }
                        }).show();
                    }

                }
            };

            handler_restartActivity.postDelayed(runnable_restartActivity, 10000);

        } else {
            linearLayout_progress_bar_main.setVisibility(GONE);
            mApplication.setMainActivityReloadCounter(0);
        }
    }

    private void checkPremiumUser() {
//        Log.w(TAG, "checkPremiumUser()");
        try {
            DatabaseReference dbRef_premium = database.getReference("/users/" + mFirebaseUser.getEmail().replace(".", "!") + "/premium/");
            dbRef_premium.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
//                        Log.i(TAG, "checkPremiumUser().onDataChange(): " + dataSnapshot.getValue());
                        if (dataSnapshot.getValue() != null) {
                            boolean premium = (boolean) dataSnapshot.getValue();
                            if (premium) {
                                premium_user = (boolean) dataSnapshot.getValue();
                                getSupportActionBar().setSubtitle(R.string.premium);
                            } else {
                                premium_user = false;
                                getSupportActionBar().setSubtitle("");
                            }
                        } else {
                            premium_user = false;
                        }

                        Handler handler_ads = new Handler();
                        handler_ads.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setupAds(!premium_user);
                            }
                        }, 3000);


                        textView_nav_header_app_version.setText(getString(R.string.version) + " " + Util.getAppVersionName(MainActivity.this) + " - " + getPremium());

                    } catch (Exception e) {
                        Log.e(TAG, "checkPremiumUser: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "checkPremiumUser(): The read failed: " + databaseError.getCode());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "checkPremiumUser(): " + e.getMessage());
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        try {
            unregisterReceiver(broadcastReceiverShutdown);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mPlusOneButton.initialize("https://play.google.com/store/apps/details?id=com.sow.gpstrackerpro", PLUS_ONE_REQUEST_CODE);
        } catch (Exception e) {

        }
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.i(TAG, "Revoke Access");
                    }
                });
    }

}