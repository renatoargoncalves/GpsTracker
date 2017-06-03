package com.sow.gpstrackerpro;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Fence;
import com.sow.gpstrackerpro.classes.GeoPlace;
import com.sow.gpstrackerpro.classes.Log;
import com.sow.gpstrackerpro.classes.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.view.View.GONE;
import static com.sow.gpstrackerpro.R.string.delete;

public class ManagePlacesActivity extends AppCompatActivity {

    private final static String TAG = "ManagePlacesActivity";
    private RelativeLayout relativeLayout_my_map;
    private Toolbar toolbar;
    private LinearLayoutManager linearLayoutManager_IamTracking;
    private RecyclerView recyclerView_my_map;
    private ArrayList<GeoPlace> placesList = new ArrayList<>();
    private int receivedUserTrackedCounter = 0;
    private LinearLayout linearLayout_progress_bar_my_map, linearLayout_nobody_in_your_map;
    private TextView textView_progress_bar_my_map;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbRef_places;
    private ValueEventListener placesListener;
    private FirebaseUser firebaseUser;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_places);

        myApplication = (MyApplication) getApplicationContext();

        firebaseUser = myApplication.getFirebaseUser();

        configureUI();

        getPlacesList();

    }


    private void configureUI() {
        // Configures the Toolbar on the MainActivity and sets the title
        toolbar = (Toolbar) findViewById(R.id.toolbar_places);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.manage_locals);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        relativeLayout_my_map = (RelativeLayout) findViewById(R.id.relativeLayout_my_map);

        linearLayoutManager_IamTracking = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager_IamTracking.setReverseLayout(true);
        linearLayoutManager_IamTracking.setStackFromEnd(true);

        recyclerView_my_map = (RecyclerView) findViewById(R.id.recyclerView_my_map);
        recyclerView_my_map.setLayoutManager(linearLayoutManager_IamTracking);

        linearLayout_progress_bar_my_map = (LinearLayout) findViewById(R.id.linearLayout_progress_bar_my_map);
        linearLayout_progress_bar_my_map.setVisibility(GONE);

        textView_progress_bar_my_map = (TextView) findViewById(R.id.textView_progress_bar_my_map);
        textView_progress_bar_my_map.setText(R.string.loading_your_data);

        linearLayout_nobody_in_your_map = (LinearLayout) findViewById(R.id.linearLayout_nobody_in_your_map);
        linearLayout_nobody_in_your_map.setVisibility(GONE);

    }


    private void getPlacesList() {
        Log.w(TAG, "getPlacesList()");
        linearLayout_progress_bar_my_map.setVisibility(View.VISIBLE);
        receivedUserTrackedCounter = 0;

        try {
            dbRef_places = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/places/");
            placesListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.i(TAG, "getPlacesList().onDataChange(): " + dataSnapshot.getValue().toString());
                        placesList.clear();
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            GeoPlace geoPlace = new GeoPlace();
                            geoPlace.setName(dsp.getKey());
                            geoPlace.setFence(dsp.getValue(Fence.class));
                            placesList.add(geoPlace);
                        }
                        populatePlacesList();
                    } catch (Exception e) {
                        placesList.clear();
                        populatePlacesList();
                        Log.e(TAG, "getPlacesList(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "getPlacesList: " + databaseError.getCode());
                    Toast.makeText(ManagePlacesActivity.this, "getPlacesList: " + databaseError.getCode(), Toast.LENGTH_LONG).show();
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "getPlacesList(): " + e.getMessage());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (placesListener != null) {
            dbRef_places.addValueEventListener(placesListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (placesListener != null) {
            dbRef_places.removeEventListener(placesListener);
        }
    }


    private void populatePlacesList() {
        Log.w(TAG, "populatePlacesList()");
        recyclerView_my_map.setAdapter(new ManagePlacesActivity.AdapterIamTracking(this, placesList));
        if (recyclerView_my_map.getAdapter().getItemCount() == 0) {
            linearLayout_nobody_in_your_map.setVisibility(View.VISIBLE);
        }
        linearLayout_progress_bar_my_map.setVisibility(GONE);
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

                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(110, 125, conf);
                Canvas canvas1 = new Canvas(bmp);

                // paint defines the text color, stroke width and size
                Paint color = new Paint();
                color.setTextSize(35);
                color.setColor(getResources().getColor(R.color.colorPrimary));

                // modify canvas
                canvas1.drawRoundRect(new RectF(0, 0, myBitmap.getWidth() + 6, myBitmap.getHeight() + 6), 6, 6, color);
                canvas1.drawBitmap(myBitmap, 3, 3, color);
                color.setStyle(Paint.Style.FILL);
                return bmp;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class AdapterIamTracking extends RecyclerView.Adapter<ManagePlacesActivity.AdapterIamTracking.ViewHolder> {
        private Context context;
        private ArrayList<GeoPlace> placesList;

        public AdapterIamTracking(Context context, ArrayList<GeoPlace> placesList) {
            this.context = context;
            this.placesList = placesList;
        }

        @Override
        public ManagePlacesActivity.AdapterIamTracking.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_place, parent, false);
            return new ManagePlacesActivity.AdapterIamTracking.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ManagePlacesActivity.AdapterIamTracking.ViewHolder holder, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ManagePlacesActivity.this, PlaceDetailsActivity.class);
                    Bundle b = new Bundle();
                    b.putString("place", placesList.get(position).getName());
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
            String mask = "+"+placesList.get(position).getFence().getOwner().replace(".","!");
            Log.i(TAG, "mask: "+mask);
            Log.i(TAG, "name: "+placesList.get(position).getName());
            holder.textView_placeName.setText(placesList.get(position).getName().replace(mask, ""));
            holder.textView_placeOwner.setText(placesList.get(position).getFence().getOwner().replace("!","."));
            holder.imageView_delete_place.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_more_details));
        }

        @Override
        public int getItemCount() {
            return ManagePlacesActivity.this.placesList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView_placeIcon, imageView_delete_place;
            TextView textView_placeName, textView_placeOwner;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView_placeIcon = (ImageView) itemView.findViewById(R.id.imageView_placeIcon);
                textView_placeName = (TextView) itemView.findViewById(R.id.textView_placeName);
                textView_placeOwner = (TextView) itemView.findViewById(R.id.textView_placeOwner);
                imageView_delete_place = (ImageView) itemView.findViewById(R.id.imageView_delete_place);
            }
        }
    }

}
