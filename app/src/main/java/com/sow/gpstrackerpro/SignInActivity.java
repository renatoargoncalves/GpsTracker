package com.sow.gpstrackerpro;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sow.gpstrackerpro.classes.Log;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Track;
import com.sow.gpstrackerpro.classes.TracksContainer;
import com.sow.gpstrackerpro.classes.User;
import com.sow.gpstrackerpro.classes.UserInfo;
import com.sow.gpstrackerpro.classes.Util;

import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sow.gpstrackerpro.R.id.textView_signed_in_user;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_LOCATION_PERMITION = 112;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMITION = 222;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private SignInButton sign_in_button;
    private LinearLayout linearLayout_progressbar_sign_in, linearLayout_terms_conditions;
    private MyApplication myApplication;
    private CheckBox checkBox_agree;
    private TextView textView_agree, textView_progressbar;
    private RelativeLayout relativeLayout_sign_in;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        try {
            myApplication = (MyApplication) getApplicationContext();
            configureUI();
            mGoogleApiClient = connectGoogleApiClient();
            mAuthListener = listenFirebaseAuthState();
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(111);
        } catch (Exception e) {
            Toast.makeText(this, "onCreate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void configureUI() {
        // Setup UI components
        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);

        textView_progressbar = (TextView) findViewById(R.id.textView_progressbar);

        linearLayout_progressbar_sign_in = (LinearLayout) findViewById(R.id.linearLayout_progress_bar_sign_in);

        linearLayout_terms_conditions = (LinearLayout) findViewById(R.id.linearLayout_terms_conditions);


        relativeLayout_sign_in = (RelativeLayout) findViewById(R.id.relativeLayout_sign_in);

        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        checkBox_agree = (CheckBox) findViewById(R.id.checkBox_agree);
        checkBox_agree.setChecked(sharedPref.getBoolean(getString(R.string.accept_terms_and_conditions), false));
        checkBox_agree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.accept_terms_and_conditions), b);
                editor.commit();
            }
        });

        textView_agree = (TextView) findViewById(R.id.textView_agree);
        textView_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, TermsAndConditionsActivity.class));
            }
        });

        // Configures the button Sign In
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox_agree.isChecked()) {
                    if (Util.isDataConnected(getApplicationContext()))
                        signIn();
                    else
                        Snackbar.make(relativeLayout_sign_in, R.string.no_network_try_again_later, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    Snackbar.make(relativeLayout_sign_in, R.string.you_need_to_accept_the_terms_and_conditions, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

    }

    private FirebaseAuth.AuthStateListener listenFirebaseAuthState() {
        try {
            FirebaseAuth.AuthStateListener localFirebaseAuthStateListener;
            mAuth = FirebaseAuth.getInstance();
            localFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        // User is signed in
                        showProgressBar(true, "");
                        Log.d(TAG, "user is signed in:" + firebaseUser.getEmail());
                        sign_in_button.setVisibility(GONE);
                        checkBox_agree.setVisibility(GONE);
                        textView_agree.setVisibility(GONE);
                        checkLocationPermissions();
                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                        showProgressBar(false, "");
                        sign_in_button.setVisibility(VISIBLE);
                        checkBox_agree.setVisibility(VISIBLE);
                        textView_agree.setVisibility(VISIBLE);
//                        textView_signed_in_user.setText("GPS Tracker");
                    }
                }
            };
            return localFirebaseAuthStateListener;
        } catch (Exception e) {
            Toast.makeText(this, "listenFirebaseAuthState: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void getUserDataFromFirebase() {
        Log.w(TAG, "getUserDataFromFirebase()");

        try {
            showProgressBar(true, getString(R.string.connecting));

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userInfo/");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        if (userInfo != null) {
                            Log.i(TAG, "getUserDataFromFirebase().onDataChange(): " + dataSnapshot.getValue().toString());
                            if (!myApplication.isAppStarted())
                                myApplication.startApplication();
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            showProgressBar(false, "");
                            finish();
                        } else {
                            Log.i(TAG, "getUserDataFromFirebase().onDataChange(): NULL");
                            newRegistration();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getUserDataFromFirebase().onDataChange(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "getUserDataFromFirebase(): " + e.getMessage());
        }
    }

    private void newRegistration() {
        Log.w(TAG, "newRegistration()");
        showProgressBar(true, getString(R.string.new_user));

        User user = new User(firebaseUser.getUid());
        UserInfo userInfo = new UserInfo(
                firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl().toString(),
                firebaseUser.getEmail()
        );

        createUserOnFirebase(user);
        createUserInfoOnFirebase(userInfo);
        createUserTrackedListOnFirebase();
//        createUserTrackingListOnFirebase();
        createUserSignInOnFirebase();
        createUserInstall();
        createCheckinOnFirebase();
    }

    private void signIn() {
        Log.w(TAG, "signIn()");
        try {
            if (Util.isGpsEnabled(this)) {
                linearLayout_terms_conditions.setVisibility(GONE);
                showProgressBar(true, getString(R.string.connecting));
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } else {
                Snackbar.make(relativeLayout_sign_in, R.string.please_turn_your_gps_on, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "signIn()" + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                } else {
                    Snackbar.make(relativeLayout_sign_in, R.string.please_use_gmail_account, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    linearLayout_terms_conditions.setVisibility(VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult()" + e.getMessage());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mAuth.addAuthStateListener(mAuthListener);
        } catch (Exception e) {
            Log.e(TAG, "onStart()" + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "onStop()" + e.getMessage());
        }
    }

    public void createCheckinOnFirebase() {

        Log.w(TAG, "updateCheckinOnFirebase()");
        try {
            String string = firebaseUser.getDisplayName() + " " + getString(R.string.is_connected);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/checkinMsg/");
            ref.setValue(string, new DatabaseReference.CompletionListener() {
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

    private void createUserOnFirebase(final User user) {
        Log.w(TAG, "createUserOnFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!"));
            ref.setValue(user, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "User could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "User created successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "createNewUserOnFirebase: " + e.getMessage());
        }
    }

    private void createUserInfoOnFirebase(UserInfo userInfo) {
        Log.w(TAG, "createUserInfoOnFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userInfo/");
            ref.setValue(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "UserInfo could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "UserInfo created successfully.");
                        getUserDataFromFirebase();
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "createUserInfoOnFirebase: " + e.getMessage());
        }
    }

    private void createUserTrackedListOnFirebase() {
        Log.w(TAG, "createUserTrackedListOnFirebase()");
        try {

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userTrackedList/" + firebaseUser.getEmail().replace(".", "!"));

            ref.setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "userTrackedList could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "userTrackedList created successfully.");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "createUserTrackedListOnFirebase: " + e.getMessage());
        }

    }

    private void createUserTrackingListOnFirebase() {
        Log.w(TAG, "createUserTrackingListOnFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userTrackingList/");
            TracksContainer tracksContainer = new TracksContainer();
            ref.setValue(tracksContainer, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "userTrackingList could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "userTrackingList created successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "createUserTrackingListOnFirebase: " + e.getMessage());
        }

    }

    private void createUserSignInOnFirebase() {
        Log.w(TAG, "createUserSignInOnFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/signIn/");

            ref.setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "userSignIn could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "userSignIn created successfully.");

                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "createUserSignInOnFirebase: " + e.getMessage());
        }

    }

    private void createUserInstall() {
        Log.w(TAG, "createUserInstall()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/installed/");

            ref.setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "createUserInstall could not be created: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "createUserInstall created successfully.");

                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "createUserInstall: " + e.getMessage());
        }
    }


    private void checkLocationPermissions() {
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(SignInActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                showProgressBar(false, "");
                ActivityCompat.requestPermissions(SignInActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMITION);
            } else {
                checkWriteExternalStoragePermissions();
            }
        } catch (Exception e) {
            Toast.makeText(this, "checkLocationPermissions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkWriteExternalStoragePermissions() {
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(SignInActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                showProgressBar(false, "");
                ActivityCompat.requestPermissions(SignInActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMITION);
            } else {
                if (Util.isDataConnected(getApplicationContext())) {
                    getUserDataFromFirebase();
                } else {
                    showProgressBar(false, "");
                    Snackbar.make(relativeLayout_sign_in, R.string.no_network_try_again_later, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "checkWriteExternalStoragePermissions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case REQUEST_LOCATION_PERMITION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        checkWriteExternalStoragePermissions();
                    } else {
                        Toast.makeText(this, "Sorry, we can´t go on without permission to your location.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
                case REQUEST_EXTERNAL_STORAGE_PERMITION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        if (Util.isDataConnected(getApplicationContext())) {
                            getUserDataFromFirebase();
                        } else {
                            showProgressBar(false, "");
                            Snackbar.make(relativeLayout_sign_in, R.string.no_network_try_again_later, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 3000);
                        }
                    } else {
                        Toast.makeText(this, "Sorry, we can´t go on without permission to write.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }

            }
        } catch (Exception e) {
            Toast.makeText(this, "onRequestPermissionsResult: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressBar(boolean show, String text) {
        if (show) {
            linearLayout_progressbar_sign_in.setVisibility(VISIBLE);
            textView_progressbar.setText(text);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (linearLayout_progressbar_sign_in.getVisibility() == VISIBLE) {
                        textView_progressbar.setText(getString(R.string.this_is_taking_longer_than_usual));
                        Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (linearLayout_progressbar_sign_in.getVisibility() == VISIBLE) {
                                    Toast.makeText(SignInActivity.this, getString(R.string.no_network_try_again_later), Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        }, 10000);
                    }
                }
            }, 15000);
        } else
            linearLayout_progressbar_sign_in.setVisibility(GONE);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.w(TAG, "firebaseAuthWithGoogle()");
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "Firebase Authentication successful!:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Firebase Authentication failed." + task.getException());
                                Toast.makeText(SignInActivity.this, "Firebase Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "firebaseAuthWithGoogle()" + e.getMessage());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void revokeAccess() {
        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Toast.makeText(SignInActivity.this, "Revoke Access", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private GoogleApiClient connectGoogleApiClient() {
        GoogleApiClient localGoogleApiClient;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        localGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        return localGoogleApiClient;
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
