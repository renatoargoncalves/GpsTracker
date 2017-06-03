package com.sow.gpstrackerpro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Log;
import com.sow.gpstrackerpro.classes.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class HandlePictureActivity extends AppCompatActivity {

    private static final String TAG = "HandlePictureActivity";
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private RelativeLayout relativeLayout_cropview, relativeLayout_final_picture;
    private int SELECT_PICTURE = 1232;
    private ImageView imageView_handle_picture;
    private CropImageView cropImageView, cropImageView_final;
    private Toolbar toolbar;
    private Button button_crop;
    private FirebaseUser firebaseUser;
    private MyApplication myApplication;
    private ImageButton button_spin;
    private LinearLayout linearLayout_progress_bar_handle_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_picture);

        myApplication = (MyApplication) getApplicationContext();

        firebaseUser = myApplication.getFirebaseUser();

        toolbar = (Toolbar) findViewById(R.id.toolbar_handle_picture);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.change_picture);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        relativeLayout_cropview = (RelativeLayout) findViewById(R.id.relativeLayout_cropview);

        button_spin = (ImageButton) findViewById(R.id.button_spin);
        button_spin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(-90);
            }
        });

        button_crop = (Button) findViewById(R.id.button_crop);
        button_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout_progress_bar_handle_picture.setVisibility(View.VISIBLE);
                final Bitmap userPicture = Bitmap.createScaledBitmap(cropImageView.getCroppedImage(), 96, 96, false);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                userPicture.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                String path = "userPictures/" + UUID.randomUUID() + ".png";
                StorageReference userPicturesRef = firebaseStorage.getReference(path);

                UploadTask uploadTask = userPicturesRef.putBytes(data);
                Log.i(TAG, "Uploading picture...");
                uploadTask.addOnSuccessListener(HandlePictureActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG, "Picture uploaded");
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        getUserInfoFromFirebase(downloadUrl.toString());
                    }
                });

                uploadTask.addOnFailureListener(HandlePictureActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
                        Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        linearLayout_progress_bar_handle_picture = (LinearLayout) findViewById(R.id.linearLayout_progress_bar_handle_picture);
        linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);

        pickImage();
    }

    public void pickImage() {

        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, SELECT_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                cropImageView = (CropImageView) findViewById(R.id.cropImageView);
                cropImageView.setImageBitmap(bitmap);
                cropImageView.setAspectRatio(2, 2);
                cropImageView.setFixedAspectRatio(true);
                cropImageView.setGuidelines(2);

            } catch (IOException e) {
                e.printStackTrace();
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

    public int pxToDp(int dp) {

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    private void getUserInfoFromFirebase(final String newIcon) {
        Log.w(TAG, "getUserInfoFromFirebase()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userInfo/");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        if (userInfo != null) {
                            deleteOldPictureFromStorage(userInfo.getIcon());
                            userInfo.setIcon(newIcon);
                            updateUserInfoOnFirebase(userInfo);
                        } else {
                            Toast.makeText(HandlePictureActivity.this, getString(R.string.could_not_update), Toast.LENGTH_SHORT).show();
                            linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getUserInfoFromFirebase().onDataChange(): " + e.getMessage());
                        Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "getUserInfoFromFirebase(): " + e.getMessage());
        }

    }

    private void deleteOldPictureFromStorage(String icon) {
        Log.w(TAG, "deleteOldPictureFromStorage: " + icon);
        try {
            String strAux = icon.replace("https://firebasestorage.googleapis.com/v0/b/personal-gps-tracker.appspot.com/o/userPictures%2F", "");
            final String fileName = strAux.substring(0, strAux.indexOf(".png")+4);
            Log.i(TAG, "filename: " + fileName);

            StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://personal-gps-tracker.appspot.com/");
            StorageReference desertRef = storageRef.child("userPictures/" + fileName);

            desertRef.delete().addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Log.i(TAG, "File " + fileName + " deleted.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "Error deleting: " + exception.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "deleteOldPictureFromStorage(): " + e.getMessage());
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
                        Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
                    } else {
                        updateNewPictureFlagOnFirebase(true);
                        Log.i(TAG, "updateUserInfoOnFirebase(): userInfo updated.");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "updateUserInfoOnFirebase(): " + e.getMessage());
            Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
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
                        Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
                    } else {
                        Log.i(TAG, "updateNewPictureFlagOnFirebase(): userInfo updated.");
                        startActivity(new Intent(HandlePictureActivity.this, SettingsActivity.class));
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "updateNewPictureFlagOnFirebase(): " + e.getMessage());
            Toast.makeText(HandlePictureActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            linearLayout_progress_bar_handle_picture.setVisibility(View.GONE);
        }
    }

}
