package com.sow.gpstrackerpro;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusOneButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Log;
import com.sow.gpstrackerpro.classes.UserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Toolbar toolbar;
    private LinearLayout linearLayout_change_profile_picture, linearLayout_progress_bar_settings;
    private ImageView imageView_profile, imageView_profile_edit_name;
    private Dialog dialog;
    private String TAG = "SettingsActivity";
    private MyApplication myApplication;
    private FirebaseUser firebaseUser;
    private TextView textView_profile_name, textView_profile_email;
    private CardView cardView_settings;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myApplication = (MyApplication) getApplicationContext();

        firebaseUser = myApplication.getFirebaseUser();

        // Connects to GoogleApiClient
        mGoogleApiClient = connectGoogleApiClient();
        mGoogleApiClient.connect();


        configureUI();

        getUserInfo(firebaseUser.getEmail().replace(".", "!"));
    }

    private void getUserInfo(String user) {
        Log.w(TAG, "getUserInfo(): " + user);

        try {
            DatabaseReference dbRef_user = database.getReference("/users/" + user + "/userInfo/");
            dbRef_user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "getUserInfo(): onDataChange: " + dataSnapshot.getValue().toString());
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        if (userInfo != null) {
                            textView_profile_name.setText(userInfo.getDisplayName());
                            loadUserPicture(userInfo.getIcon());
                        } else {
                            Log.e(TAG, "getUserInfo: desc is NULL");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getUserInfo(): " + e.getMessage());
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

    private void loadUserPicture(String iconUrl) {
        Log.w(TAG, "loadUserPicture()");
        SettingsActivity.MyAsync obj = new SettingsActivity.MyAsync(iconUrl) {
            @Override
            protected void onPostExecute(Bitmap bmp) {
                super.onPostExecute(bmp);
                saveUserPictureToDisk(bmp, "esse");
                Log.i(TAG, "loadUserPicture().onPostExecute()");
                imageView_profile.setImageBitmap(bmp);
                linearLayout_progress_bar_settings.setVisibility(View.GONE);
                cardView_settings.setVisibility(View.VISIBLE);
            }
        };
        obj.execute();
    }


    private void configureUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        linearLayout_change_profile_picture = (LinearLayout) findViewById(R.id.linearLayout_change_profile_picture);
        linearLayout_change_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangePictureFlow();
            }
        });

        imageView_profile = (ImageView) findViewById(R.id.imageView_profile);
        imageView_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangePictureFlow();
            }
        });

        imageView_profile_edit_name = (ImageView) findViewById(R.id.imageView_profile_edit_name);
        imageView_profile_edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangeNameFlow();
            }
        });

        textView_profile_name = (TextView) findViewById(R.id.textView_profile_name);

        textView_profile_email = (TextView) findViewById(R.id.textView_profile_email);
        textView_profile_email.setText(firebaseUser.getEmail());

        cardView_settings = (CardView) findViewById(R.id.cardView_settings);
        cardView_settings.setVisibility(View.GONE);

        linearLayout_progress_bar_settings = (LinearLayout) findViewById(R.id.linearLayout_progress_bar_settings);
        linearLayout_progress_bar_settings.setVisibility(View.VISIBLE);

    }

    private void startChangePictureFlow() {
        startActivity(new Intent(SettingsActivity.this, HandlePictureActivity.class));
    }

    private void startChangeNameFlow() {
        showChangeNameDialog();
    }

    private void showChangeNameDialog() {

        dialog = new Dialog(SettingsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_edittext);

        TextView textView_track_back_title = (TextView) dialog.findViewById(R.id.textView_dialog_editText_title);
        TextView textView_track_back_desc = (TextView) dialog.findViewById(R.id.textView_dialog_editText_text);
        TextView btn_track_back_done = (TextView) dialog.findViewById(R.id.textView_dialog_editText_ok);
        TextView btn_track_back_cancel = (TextView) dialog.findViewById(R.id.textView_dialog_editText_cancel);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText_dialog_editText);

        textView_track_back_title.setText(R.string.dialog_change_profile_name_title);
        textView_track_back_desc.setText(getResources().getString(R.string.type_your_name));

        btn_track_back_done.setText(getString(R.string.change).toUpperCase());
        btn_track_back_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() > 3) {
                    getUserInfoFromFirebase(editText.getText().toString());
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.invalid_name), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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

    private void getUserInfoFromFirebase(final String newName) {
        Log.w(TAG, "checkPhoneModel()");
        try {
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userInfo/");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        if (userInfo != null) {
                            userInfo.setDisplayName(newName);
                            updateUserInfoOnFirebase(userInfo);
                        } else {
                            Toast.makeText(SettingsActivity.this, getString(R.string.could_not_update), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getUserInfoFromFirebase().onDataChange(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "checkPhoneModel(): " + e.getMessage());
        }

    }

    private void updateUserInfoOnFirebase(final UserInfo userInfo) {
        Log.w(TAG, "updateUserInfoOnFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userInfo/");

            ref.setValue(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i(TAG, "updateUserInfoOnFirebase(): Could not update userInfo: " + databaseError.getMessage());
                    } else {
                        Log.i(TAG, "updateUserInfoOnFirebase(): userInfo updated.");
                        textView_profile_name.setText(userInfo.getDisplayName());
                        textView_profile_email.setText(firebaseUser.getEmail());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "updateUserInfoOnFirebase(): " + e.getMessage());
        }

    }

    private GoogleApiClient connectGoogleApiClient() {
        Log.w(TAG, "connectGoogleApiClient()");
        GoogleApiClient localGoogleApiClient;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        localGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        return localGoogleApiClient;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public class MyAsync extends AsyncTask<Void, Void, Bitmap> {

        private String src;

        public MyAsync(String src) {
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
                Bitmap aux = Bitmap.createScaledBitmap(myBitmap, 92, 92, false);

                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//                Bitmap bmp = Bitmap.createBitmap(110, 125, conf);
                Bitmap bmp = Bitmap.createBitmap(96, 96, conf);
                Canvas canvas1 = new Canvas(bmp);

                // paint defines the text color, stroke width and size
                Paint color = new Paint();
                color.setTextSize(35);
                color.setColor(getResources().getColor(R.color.colorPrimary));

                // modify canvas
//                canvas1.drawRoundRect(new RectF(0, 0, myBitmap.getWidth() + 6, myBitmap.getHeight() + 6), 6, 6, color);

                canvas1.drawRoundRect(new RectF(0, 0, myBitmap.getWidth(), myBitmap.getHeight()), 0, 0, color);
                canvas1.drawBitmap(aux, 2, 2, color);

//                canvas1.drawBitmap(myBitmap, 3, 3, color);
                color.setStyle(Paint.Style.FILL);
                return bmp;
            } catch (IOException e) {
                e.printStackTrace();
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

}
