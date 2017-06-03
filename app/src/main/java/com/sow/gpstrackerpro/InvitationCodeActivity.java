package com.sow.gpstrackerpro;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Fence;
import com.sow.gpstrackerpro.classes.GeoPlace;
import com.sow.gpstrackerpro.classes.Invite;
import com.sow.gpstrackerpro.classes.Log;

public class InvitationCodeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editText_code_1, editText_code_2;
    private Button button_code_done;
    private LinearLayout linearLayout_progress_bar_invitation_code;
    private String TAG = "InvitationCodeActivity";
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private MyApplication myApplication;
    private RelativeLayout relativeLayout_invitation_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_code);

        myApplication = (MyApplication) getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.toolbar_invitation_code);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.invitation_code);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        relativeLayout_invitation_code = (RelativeLayout) findViewById(R.id.relativeLayout_invitation_code);

        linearLayout_progress_bar_invitation_code = (LinearLayout) findViewById(R.id.linearLayout_progress_bar_invitation_code);
        linearLayout_progress_bar_invitation_code.setVisibility(View.GONE);

        editText_code_1 = (EditText) findViewById(R.id.editText_code_1);
        editText_code_1.requestFocus();
        editText_code_1.setShowSoftInputOnFocus(true);
        editText_code_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 3)
                    editText_code_2.requestFocus();

            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        editText_code_2 = (EditText) findViewById(R.id.editText_code_2);
        editText_code_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 3) {
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    button_code_done.setEnabled(true);
                    button_code_done.requestFocus();
                }
            }
        });

        button_code_done = (Button) findViewById(R.id.button_code_done);
        button_code_done.setFocusableInTouchMode(true);
        button_code_done.setEnabled(false);
        button_code_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout_progress_bar_invitation_code.setVisibility(View.VISIBLE);
                button_code_done.setEnabled(false);

                String code = editText_code_1.getText().toString()+"-"+editText_code_2.getText().toString();

                searchInvitation(code);
            }
        });
    }

    private void searchInvitation(final String code) {
        Log.w(TAG, "getInviter()");
        try {
            DatabaseReference dbRef_getInvite = database.getReference("/invites/" + code);
            dbRef_getInvite.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Invite invite = dataSnapshot.getValue(Invite.class);
                    if (invite != null) {
                        Log.i(TAG, "getInviter().onDataChange(): " + invite.getUser());
                        addInviterToMyTrackedList(invite.getUser());
                        addMeToInvitersTrackedList(invite.getUser());

                        addInviterPlacesInMyPlaces(invite.getUser());
                        addMyPlacesInInviterPlaces(invite.getUser());

                        deleteInvitation(code, invite.getUser());
                    } else {
                        Snackbar.make(relativeLayout_invitation_code, R.string.invalid_invitation_code, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                linearLayout_progress_bar_invitation_code.setVisibility(View.GONE);
                                editText_code_1.setText("");
                                editText_code_2.setText("");
                                editText_code_1.requestFocus();
                            }
                        }).show();

                        Log.i(TAG, "getInviter().onDataChange(): NULL");
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

    private void deleteInvitation(String invitationId, final String user) {
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
                        linearLayout_progress_bar_invitation_code.setVisibility(View.GONE);
                        Intent intent = new Intent(InvitationCodeActivity.this, MainActivity.class);
                        Bundle b = new Bundle();
                        b.putString("user", user);
                        intent.putExtras(b);
                        startActivity(intent);
                        Toast.makeText(InvitationCodeActivity.this, getString(R.string.invitation_found), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deleteInvitation: " + e.getMessage());
        }
    }


    private void addMeToInvitersTrackedList(String inviter) {
        Log.w(TAG, "addMeToInvitersTrackedList()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + inviter + "/userTrackedList/" + myApplication.getFirebaseUser().getEmail().replace(".", "!"));
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


    private void addInviterToMyTrackedList(String user) {
        Log.w(TAG, "addInviterToMyTrackedList()");
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("/users/" + myApplication.getFirebaseUser().getEmail().replace(".", "!") + "/userTrackedList/" + user);
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
                                createPlace(myApplication.getFirebaseUser().getEmail().replace(".", "!"), geoPlace.getName(), geoPlace.getFence());
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
            DatabaseReference dbRef = database.getReference("/users/" + myApplication.getFirebaseUser().getEmail().replace(".", "!") + "/places/");
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.i(TAG, "addInviterPlacesInMyPlaces().onDataChange(): " + dataSnapshot.getValue().toString());
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            GeoPlace geoPlace = new GeoPlace();
                            geoPlace.setName(dsp.getKey());
                            geoPlace.setFence(dsp.getValue(Fence.class));

                            if (geoPlace.getFence().getOwner().equals(myApplication.getFirebaseUser().getEmail().replace(".", "!"))) {
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
                    }
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "createPlace: " + e.getMessage());
        }

    }

}
