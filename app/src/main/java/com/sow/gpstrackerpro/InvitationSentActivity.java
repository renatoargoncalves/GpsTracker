package com.sow.gpstrackerpro;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Invite;
import com.sow.gpstrackerpro.classes.Log;

import java.util.Locale;
import java.util.Random;

import static android.R.attr.id;


public class InvitationSentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String TAG = "InvitationSentActivity";
    private String random_code;
    private TextView textView_random_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_sent);

        toolbar = (Toolbar) findViewById(R.id.invitation_sent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.done);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        random_code = getIntent().getStringExtra("random_code");

        textView_random_code = (TextView) findViewById(R.id.textView_invitation_sent_3);
        textView_random_code.setText(random_code);

        Button button_invitation_sent = (Button) findViewById(R.id.button_invitation_sent);
        button_invitation_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InvitationSentActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }


}
