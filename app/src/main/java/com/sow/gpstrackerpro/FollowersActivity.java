package com.sow.gpstrackerpro;

import android.app.Dialog;
import android.content.Context;
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

import com.google.firebase.auth.FirebaseUser;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.classes.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.view.View.GONE;


public class FollowersActivity extends AppCompatActivity {

    private final static String TAG = "FollowersActivity";
    private RelativeLayout relativeLayout_my_map;
    private Toolbar toolbar;
    private LinearLayoutManager linearLayoutManager_IamTracking;
    private RecyclerView recyclerView_my_map;
    private ArrayList<UserRow> userTrackedList = new ArrayList<>();
//    private int receivedUserTrackedCounter = 0;
    private LinearLayout linearLayout_progress_bar_my_map, linearLayout_nobody_in_your_map;
    private TextView textView_progress_bar_my_map;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbRef_trackedList;
    private ValueEventListener trackedListListener;
    private FirebaseUser firebaseUser;
    private MyApplication myApplication;
    private long userTrackedListQty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        myApplication = (MyApplication) getApplicationContext();

        firebaseUser = myApplication.getFirebaseUser();

        configureUI();

        getUserTrackedList();

    }

    private void configureUI() {
        // Configures the Toolbar on the MainActivity and sets the title
        toolbar = (Toolbar) findViewById(R.id.toolbar_my_map);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.my_map);
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


    private void getUserTrackedList() {
        Log.w(TAG, "getUserTrackedList()");
        linearLayout_progress_bar_my_map.setVisibility(View.VISIBLE);
//        receivedUserTrackedCounter = 0;

        try {
            dbRef_trackedList = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userTrackedList/");
            trackedListListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.i(TAG, "getUserTrackedList().onDataChange(): " + dataSnapshot.getValue().toString());
                        userTrackedList.clear();
                        userTrackedListQty = dataSnapshot.getChildrenCount();
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            getUserInfo(dsp.getKey());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getUserTrackedList(): " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "getUserTrackedList: " + databaseError.getCode());
                    Toast.makeText(FollowersActivity.this, "getUserTrackedList: " + databaseError.getCode(), Toast.LENGTH_LONG).show();
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "getUserTrackedList(): " + e.getMessage());
        }
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
                            UserRow userRow = new UserRow();
                            userRow.setUserInfo(userInfo);
                            loadUserPicture(userRow.getUserInfo().getIcon(), userRow);
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


    @Override
    protected void onStart() {
        super.onStart();
        dbRef_trackedList.addValueEventListener(trackedListListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (trackedListListener != null) {
            dbRef_trackedList.removeEventListener(trackedListListener);
        }
    }

    private void loadUserPicture(String iconUrl, final UserRow userRow) {
        Log.w(TAG, "loadUserPicture()");
        textView_progress_bar_my_map.setText(R.string.loading_pictures);
        MyAsync obj = new MyAsync(iconUrl) {
            @Override
            protected void onPostExecute(Bitmap bmp) {
                super.onPostExecute(bmp);
                Log.i(TAG, "loadUserPicture().onPostExecute()");
                userRow.setUserPicture(bmp);
                userTrackedList.add(userRow);
//                receivedUserTrackedCounter++;
//                Log.i(TAG, "receivedUserTrackedCounter: " + receivedUserTrackedCounter);
//                Log.i(TAG, "userTrackedList.size(): " + userTrackedList.size());

                if (userTrackedListQty == userTrackedList.size())
                    populateUserTrackingList();
            }
        };
        obj.execute();
    }

    private void populateUserTrackingList() {
        Log.w(TAG, "populateUserTrackingList()");
        recyclerView_my_map.setAdapter(new AdapterIamTracking(this, userTrackedList));
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

    private class AdapterIamTracking extends RecyclerView.Adapter<AdapterIamTracking.ViewHolder> {
        private Context context;
        private ArrayList<UserRow> userTrackedList;

        public AdapterIamTracking(Context context, ArrayList<UserRow> userTrackedList) {
            this.context = context;
            this.userTrackedList = userTrackedList;

            for (int i = 0; i < this.userTrackedList.size(); i++) {
                if(this.userTrackedList.get(i).getUserInfo().getEmail().replace(".","!").equals(firebaseUser.getEmail().replace(".","!"))) {
                    this.userTrackedList.remove(i);
                    break;
                }
            }
        }

        @Override
        public AdapterIamTracking.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_follower, parent, false);
            return new AdapterIamTracking.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final AdapterIamTracking.ViewHolder holder, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            holder.imageView_userPicture.setImageBitmap(userTrackedList.get(position).getUserPicture());
            holder.textView_name_my_map.setText(userTrackedList.get(position).getUserInfo().getDisplayName());
            holder.textView_email_my_map.setText(userTrackedList.get(position).getUserInfo().getEmail());
            if (!userTrackedList.get(position).getUserInfo().getEmail().replace(".","!").equals(firebaseUser.getEmail().replace(".","!"))) {
                holder.imageView_delete_user.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_delete));
                holder.imageView_delete_user.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDeleteConfirmation(position);
                    }
                });
            } else {
                holder.imageView_delete_user.setVisibility(GONE);
            }
        }

        @Override
        public int getItemCount() {
            return FollowersActivity.this.userTrackedList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView_userPicture, imageView_delete_user;
            TextView textView_name_my_map, textView_email_my_map;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView_userPicture = (ImageView) itemView.findViewById(R.id.imageView_user_picture_my_map);
                textView_name_my_map = (TextView) itemView.findViewById(R.id.textView_name_my_map);
                textView_email_my_map = (TextView) itemView.findViewById(R.id.textView_email_my_map);
                imageView_delete_user = (ImageView) itemView.findViewById(R.id.imageView_delete_user);
            }
        }
    }

    private void showDeleteConfirmation(final int position) {
        final Dialog dialog = new Dialog(FollowersActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog);

        TextView textView_title = (TextView) dialog.findViewById(R.id.textView_dialog_editText_title);
        textView_title.setText(R.string.delete_user);
        TextView textView_desc = (TextView) dialog.findViewById(R.id.textView_dialog_editText_text);
        textView_desc.setText(R.string.your_contacts_will_not_be_able_to_track_each_other);
        TextView btn_confirm = (TextView) dialog.findViewById(R.id.textView_dialog_editText_ok);
        TextView btn_cancel = (TextView) dialog.findViewById(R.id.textView_dialog_editText_cancel);

        btn_confirm.setText(R.string.ok);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserFromMyTrackedList(position);
                deleteMeFromUserTrackedList(position);
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

    private void deleteUserFromMyTrackedList(int position) {
        Log.i(TAG, "deleteUserFromMyTrackedList(): ");

        String user = userTrackedList.get(position).getUserInfo().getEmail().replace(".", "!");

        try {
            DatabaseReference dbRef = database.getReference("/users/" + firebaseUser.getEmail().replace(".", "!") + "/userTrackedList/" + user);
            dbRef.setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "deleteUserFromMyTrackedList could not be executed.");
                    } else {
                        Log.i(TAG, "deleteUserFromMyTrackedList executed successfully.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deleteUserFromMyTrackedList(): " + e.getMessage());
        }

    }

    private void deleteMeFromUserTrackedList (int position) {
        Log.i(TAG, "deleteUserFromMyTrackedList(): ");

        String user = userTrackedList.get(position).getUserInfo().getEmail().replace(".", "!");

        try {
            DatabaseReference dbRef = database.getReference("/users/" + user + "/userTrackedList/" + firebaseUser.getEmail().replace(".", "!"));
            dbRef.setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "deleteUserFromMyTrackedList could not be executed.");
                    } else {
                        Log.i(TAG, "deleteUserFromMyTrackedList executed successfully.");
                        getUserTrackedList();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deleteUserFromMyTrackedList(): " + e.getMessage());
        }

    }

    private class UserRow {

        private Bitmap userPicture;
        private UserInfo userInfo;

        public UserRow(Bitmap userPicture, UserInfo userInfo) {
            this.userPicture = userPicture;
            this.userInfo = userInfo;
        }

        public UserRow() {
        }

        public Bitmap getUserPicture() {
            return userPicture;
        }

        public void setUserPicture(Bitmap userPicture) {
            this.userPicture = userPicture;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }
    }

}


