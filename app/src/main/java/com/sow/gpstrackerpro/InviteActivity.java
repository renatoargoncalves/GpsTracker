package com.sow.gpstrackerpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import static com.sow.gpstrackerpro.R.id.textView_invite_1;

public class InviteActivity extends AppCompatActivity {

    private static final String TAG = "InviteActivity";
    private final static int REQUEST_INVITE_WHATSAPP = 6161;
    private Toolbar toolbar;
    private String random_code;
    private Button button_send_invite;
    private DatabaseReference dbRef_createInvite;
    private MyApplication myApplication;
    private LinearLayout linearLayout_key;
    private TextView textView_invite_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        myApplication = (MyApplication) getApplicationContext();

        configureUi();

        random_code = generateRandomCode(6);

        TextView textView_random_code = (TextView) findViewById(R.id.textView_random_code);
        textView_random_code.setText(random_code);

        createInviteOnFirebase(random_code);

        button_send_invite = (Button) findViewById(R.id.button_send_invite);
        button_send_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callShareIntent();
            }
        });

    }

    private void configureUi() {
        // Configures Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_invite);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.send_invite);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        textView_invite_1 = (TextView) findViewById(R.id.textView_invite_1);
        textView_invite_1.setVisibility(View.VISIBLE);

        linearLayout_key = (LinearLayout) findViewById(R.id.linearLayout_key);
        linearLayout_key.setVisibility(View.VISIBLE);
    }

    private String generateRandomCode(int lenght) {
        return getRandomString(lenght);
    }

    private static final String ALLOWED_CHARACTERS = "0123456789QWERTYUIOPASDFGHJKLZXCVBNM";

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i) {
            if (i == 3)
                sb.append("-");
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        return sb.toString();
    }

    private void callShareIntent() {
        textView_invite_1.setVisibility(View.GONE);
        linearLayout_key.setVisibility(View.GONE);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);

        String locale = Locale.getDefault().getLanguage();
        String message =
                getString(R.string.invitation_message1) +
                        " " +
                        getString(R.string.invitation_message2) +
                        "\n\n" +
                        getString(R.string.invitation_message3) +
                        " " +
                        random_code +
                        "\n\n" +
                        getString(R.string.invitation_message4) +
                        "\n" +
                        "https://play.google.com/store/apps/details?id=com.sow.gpstrackerpro&hl="+
                        locale;
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        startActivityForResult(sendIntent, REQUEST_INVITE_WHATSAPP);
    }

    private void createInviteOnFirebase(String code) {
        Log.w(TAG, "createInviteOnFirebase()");
        try {

            Invite invite = new Invite();
            invite.setUser(myApplication.getFirebaseUser().getEmail().replace(".", "!"));
            invite.setInv_date(System.currentTimeMillis());


            FirebaseDatabase database = FirebaseDatabase.getInstance();
            dbRef_createInvite = database.getReference("/invites/" + code);
            dbRef_createInvite.setValue(invite, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "createInviteOnFirebase NOT executed:" + databaseError.getMessage());
                        Toast.makeText(InviteActivity.this, "createInviteOnFirebase NOT executed:", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "createInviteOnFirebase executed successfully: " + id);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "createInviteOnFirebase: " + e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        try {
            textView_invite_1.setVisibility(View.VISIBLE);
            linearLayout_key.setVisibility(View.VISIBLE);
            Intent i = new Intent(this, InvitationSentActivity.class);
            i.putExtra("random_code", random_code);
            startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: " + e.getMessage());
        }
    }

}
