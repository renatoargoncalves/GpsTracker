package com.sow.gpstrackerpro;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Fence;
import com.sow.gpstrackerpro.classes.Log;

import static android.view.View.GONE;
import static com.sow.gpstrackerpro.R.id.map_place_details;
import static com.sow.gpstrackerpro.R.string.user;

public class PlaceDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView textView_place_name, textView_place_owner, textView_place_area;
    private Toolbar toolbar;
    private String TAG = "PlaceDetailsActivity";
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser firebaseUser;
    private MyApplication myApplication;
    private Fence fence;
    private GoogleMap mMap;
    private Bundle bundle;
    private CircleOptions circleOptions_createFence;
    private Circle mapCircle_createFence;
    private SupportMapFragment mapFragment;
    private Marker marker;
    private LinearLayout linearLayout_delete_place;
    private Switch switch_show_this_place_on_my_map, switch_show_notification_from_this_place;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        myApplication = (MyApplication) getApplicationContext();

        firebaseUser = myApplication.getFirebaseUser();

        configureUI();

    }

    private void configureUI() {

        // Configures the Toolbar on the MainActivity and sets the title
        toolbar = (Toolbar) findViewById(R.id.toolbar_place_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.place_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map_place_details);
        mapFragment.getMapAsync(this);

        textView_place_name = (TextView) findViewById(R.id.textView_place_name);
        textView_place_owner = (TextView) findViewById(R.id.textView_place_owner);
        textView_place_area = (TextView) findViewById(R.id.textView_place_area);

        linearLayout_delete_place = (LinearLayout) findViewById(R.id.linearLayout_delete_place);
        linearLayout_delete_place.setVisibility(GONE);
        linearLayout_delete_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mask = "+" + fence.getOwner().replace(".", "!");
                showPlaceDeleteConfirmation(fence.getName(), fence.getName().replace(mask, ""));
            }
        });

        switch_show_this_place_on_my_map = (Switch) findViewById(R.id.switch_show_this_place_on_my_map);
        switch_show_this_place_on_my_map.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showThisPlaceOnMyMap(b);
            }
        });

        switch_show_notification_from_this_place = (Switch) findViewById(R.id.switch_show_notification_from_this_place);
        switch_show_notification_from_this_place.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showNotificationFromThisPlace(b);
            }
        });
    }

    private void showThisPlaceOnMyMap(boolean b) {
        try {
            fence.setShowOnMap(b);

            DatabaseReference dbRef_place = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/places/" + fence.getName());
            dbRef_place.setValue(fence, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        Log.i(TAG, "showThisPlaceOnMyMap() executed sucessfully!");
                    } else {
                        Log.e(TAG, "showThisPlaceOnMyMap(): " + databaseError.getDetails());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getPlaceDetails(): " + e.getMessage());
        }
    }

    private void showNotificationFromThisPlace(boolean b) {
        try {
            fence.setShowNotification(b);

            DatabaseReference dbRef_place = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/places/" + fence.getName());
            dbRef_place.setValue(fence, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        Log.i(TAG, "showThisPlaceOnMyMap() executed sucessfully!");
                    } else {
                        Log.e(TAG, "showThisPlaceOnMyMap(): " + databaseError.getDetails());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getPlaceDetails(): " + e.getMessage());
        }
    }

    private void getPlaceDetails(String place) {
        Log.w(TAG, "getPlaceDetails(): " + place);
        try {
            DatabaseReference dbRef_place = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/places/" + place);
            ValueEventListener placeListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.w(TAG, "getPlaceDetails().onDataChange(): " + dataSnapshot.getValue().toString());
                        Fence myFence = dataSnapshot.getValue(Fence.class);
                        if (myFence != null) {
                            fence = myFence;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fence.getLat(), fence.getLng()), 17));
                            showFenceDetais();
                            drawFence();
                            linearLayout_delete_place.setVisibility(View.VISIBLE);

                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getPlaceDetails(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "getPlaceDetails: " + databaseError.getCode());
                    Toast.makeText(PlaceDetailsActivity.this, "error: getPlaceDetails: " + databaseError.getCode(), Toast.LENGTH_LONG).show();
                }
            };

            dbRef_place.addListenerForSingleValueEvent(placeListener);
        } catch (Exception e) {
            Log.e(TAG, "getPlaceDetails(): " + e.getMessage());
        }

    }

    private void showFenceDetais() {
        Log.w(TAG, "showFenceDetais(): " + fence.getName());
        String mask = "+" + fence.getOwner().replace(".", "!");
        textView_place_name.setText(fence.getName().replace(mask, ""));
        textView_place_owner.setText(getString(R.string.created_by) + " " + fence.getOwner().replace("!", "."));
        textView_place_area.setText(getString(R.string.area) + " " + fence.getRadius() + " m");
        try {
            if (fence.isShowOnMap())
                switch_show_this_place_on_my_map.setChecked(true);
            else
                switch_show_this_place_on_my_map.setChecked(false);
        } catch (Exception e) {
            switch_show_this_place_on_my_map.setChecked(false);
        }

        try {
            if (fence.isShowNotification())
                switch_show_notification_from_this_place.setChecked(true);
            else
                switch_show_notification_from_this_place.setChecked(false);
        } catch (Exception e) {
            switch_show_notification_from_this_place.setChecked(false);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.w(TAG, "onMapReady");
        mMap = googleMap;
        bundle = getIntent().getExtras();
        if (bundle != null) {
            getPlaceDetails(bundle.getString("place"));
        }
    }

    private void drawFence() {
        final LatLng location = new LatLng(fence.getLat(), fence.getLng());

        circleOptions_createFence = new CircleOptions()
                .center(location)
                .radius(fence.getRadius())
                .strokeColor(0x772E2E2E)
                .strokeWidth(1.8f)
                .fillColor(0x77AAAAAA);

        mapCircle_createFence = mMap.addCircle(circleOptions_createFence);

        marker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .visible(true));

    }

    private void showPlaceDeleteConfirmation(final String place, final String placeName) {
        final Dialog dialog = new Dialog(PlaceDetailsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog);

        TextView textView_title = (TextView) dialog.findViewById(R.id.textView_dialog_editText_title);
        textView_title.setText(R.string.delete_place);
        TextView textView_desc = (TextView) dialog.findViewById(R.id.textView_dialog_editText_text);
        textView_desc.setText(getString(R.string.confirm_delete_place, placeName));
        TextView btn_confirm = (TextView) dialog.findViewById(R.id.textView_dialog_editText_ok);
        TextView btn_cancel = (TextView) dialog.findViewById(R.id.textView_dialog_editText_cancel);

        btn_confirm.setText(R.string.ok);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fence.getOwner().replace(".","!").equals(firebaseUser.getEmail().replace(".","!"))) {
                    deletePlaceFromMyTrackedList(place, placeName);
                } else {
                    deletePlaceFromMyPlaces(place, placeName);
                }
                dialog.dismiss();
            }
        });


        btn_cancel.setText(R.string.cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deletePlaceFromMyPlaces(String place, final String placeName) {
        Log.i(TAG, "deletePlace(): user: " + user + " place: " + place);
        try {
            DatabaseReference dbRef = database.getReference("/users/" + firebaseUser.getEmail().replace(".","!") + "/places/" + place);
            dbRef.setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "deletePlace could not be executed.");
                    } else {
                        Log.i(TAG, "deletePlace executed successfully.");
                        Toast.makeText(PlaceDetailsActivity.this, getString(R.string.place_delete_successfull, placeName), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deletePlace(): " + e.getMessage());
        }
    }

    private void deletePlace(String user, final String place, final String placeName) {
        Log.i(TAG, "deletePlace(): user: " + user + " place: " + place);
        try {
            DatabaseReference dbRef = database.getReference("/users/" + user + "/places/" + place);
            dbRef.setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "deletePlace could not be executed.");
                    } else {
                        Log.i(TAG, "deletePlace executed successfully.");
                        Toast.makeText(PlaceDetailsActivity.this, getString(R.string.place_delete_successfull, placeName), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deletePlace(): " + e.getMessage());
        }
    }

    private void deletePlaceFromMyTrackedList(final String place, final String placeName) {
        Log.i(TAG, "deleteUserFromMyTrackedList(): " + place);

        try {
            DatabaseReference dbRef = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userTrackedList/");
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        deletePlace(dsp.getKey(), place, placeName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deleteUserFromMyTrackedList(): " + e.getMessage());
        }

    }

}
