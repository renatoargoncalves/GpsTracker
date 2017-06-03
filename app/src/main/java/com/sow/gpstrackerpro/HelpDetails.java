package com.sow.gpstrackerpro;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sow.gpstrackerpro.classes.Log;

import java.util.Locale;

import static android.R.attr.y;

public class HelpDetails extends AppCompatActivity {

    private static final String TAG = "HelpDetails";
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Toolbar toolbar;
    private TextView
            textView_help_title,
            textView_help_1,
            textView_help_2,
            textView_help_3,
            textView_help_4,
            textView_help_5,
            textView_help_6,
            textView_help_7,
            textView_help_8;

    private ImageView
            imageView_help_1,
            imageView_help_2,
            imageView_help_3,
            imageView_help_4,
            imageView_help_5,
            imageView_help_6,
            imageView_help_7,
            imageView_help_8;

    private int item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_details);

        configureUi();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            setupHelpScreen(b.getInt("item"));
            Log.i(TAG, "Received: " + b.getInt("item"));
        }
    }

    private void configureUi() {
        textView_help_title = (TextView) findViewById(R.id.textView_help_title);
        textView_help_1 = (TextView) findViewById(R.id.textView_help_1);
        textView_help_2 = (TextView) findViewById(R.id.textView_help_2);
        textView_help_3 = (TextView) findViewById(R.id.textView_help_3);
        textView_help_4 = (TextView) findViewById(R.id.textView_help_4);
        textView_help_5 = (TextView) findViewById(R.id.textView_help_5);
        textView_help_6 = (TextView) findViewById(R.id.textView_help_6);
        textView_help_7 = (TextView) findViewById(R.id.textView_help_7);
        textView_help_8 = (TextView) findViewById(R.id.textView_help_8);

        imageView_help_1 = (ImageView) findViewById(R.id.imageView_help_1);
        imageView_help_2 = (ImageView) findViewById(R.id.imageView_help_2);
        imageView_help_3 = (ImageView) findViewById(R.id.imageView_help_3);
        imageView_help_4 = (ImageView) findViewById(R.id.imageView_help_4);
        imageView_help_5 = (ImageView) findViewById(R.id.imageView_help_5);
        imageView_help_6 = (ImageView) findViewById(R.id.imageView_help_6);
        imageView_help_7 = (ImageView) findViewById(R.id.imageView_help_7);
        imageView_help_8 = (ImageView) findViewById(R.id.imageView_help_8);

        // Configures the Toolbar on the MainActivity and sets the title
        toolbar = (Toolbar) findViewById(R.id.toolbar_help_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

    }

    private void setupHelpScreen(int item) {
        this.item = item;
        switch (item) {
            case 0:
                setupHelp_how_it_works();
                break;
            case 1:
                setupHelp_how_to_invite_people_to_my_map();
                break;
            case 2:
                setupHelp_how_to_accept_invitations();
                break;
            case 3:
                setupHelp_how_to_add_places_on_the_map();
                break;
            case 4:
                setupHelp_why_my_contacts_dont_move();
                break;
            case 5:
                setupHelp_why_the_location_is_not_accurate();
                break;
            case 6:
                break;
            case 7:
                break;

        }

    }

    private void setupHelp_how_it_works() {
        textView_help_title.setVisibility(View.VISIBLE);
        textView_help_title.setText(getString(R.string.how_it_works));

        textView_help_1.setVisibility(View.VISIBLE);
        textView_help_1.setText(getString(R.string.how_it_works_1));
        imageView_help_1.setVisibility(View.GONE);

        textView_help_2.setVisibility(View.VISIBLE);
        textView_help_2.setText(getString(R.string.how_it_works_2));
        imageView_help_2.setVisibility(View.GONE);

        textView_help_3.setVisibility(View.VISIBLE);
        textView_help_3.setText(getString(R.string.how_it_works_3));
        imageView_help_3.setVisibility(View.GONE);

        textView_help_4.setVisibility(View.VISIBLE);
        textView_help_4.setText(getString(R.string.how_it_works_4));
        imageView_help_4.setVisibility(View.GONE);

        textView_help_5.setVisibility(View.VISIBLE);
        textView_help_5.setText(getString(R.string.how_it_works_5));
        imageView_help_5.setVisibility(View.GONE);

        textView_help_6.setVisibility(View.GONE);
        textView_help_6.setText("");
        imageView_help_6.setVisibility(View.GONE);

        textView_help_7.setVisibility(View.GONE);
        textView_help_7.setText("");
        imageView_help_7.setVisibility(View.GONE);

        textView_help_8.setVisibility(View.GONE);
        textView_help_8.setText("");
        imageView_help_8.setVisibility(View.GONE);
    }

    private void setupHelp_how_to_invite_people_to_my_map() {
        textView_help_title.setVisibility(View.VISIBLE);
        textView_help_title.setText(getString(R.string.how_to_add_users_on_the_map));

        textView_help_1.setVisibility(View.VISIBLE);
        textView_help_1.setText(getString(R.string.how_to_add_users_on_the_map_1));
        loadImageView(imageView_help_1, item + "/add_user_1.png");

        textView_help_2.setVisibility(View.VISIBLE);
        textView_help_2.setText(getString(R.string.how_to_add_users_on_the_map_2));
        loadImageView(imageView_help_2, item + "/add_user_2.png");

        textView_help_3.setVisibility(View.VISIBLE);
        textView_help_3.setText(getString(R.string.how_to_add_users_on_the_map_3));
        loadImageView(imageView_help_3, item + "/add_user_3.png");

        textView_help_4.setVisibility(View.VISIBLE);
        textView_help_4.setText(getString(R.string.how_to_add_users_on_the_map_4));
        loadImageView(imageView_help_4, item + "/add_user_4.png");

        textView_help_5.setVisibility(View.VISIBLE);
        textView_help_5.setText(getString(R.string.how_to_add_users_on_the_map_5));
        loadImageView(imageView_help_5, item + "/add_user_5.png");

        textView_help_6.setVisibility(View.VISIBLE);
        textView_help_6.setText(getString(R.string.how_to_add_users_on_the_map_6));
        loadImageView(imageView_help_6, item + "/add_user_6.png");

        textView_help_7.setVisibility(View.GONE);
        textView_help_7.setText("");
//        loadImageView(imageView_help_7, language + "/"+item+"/add_user_7.png");

        textView_help_8.setVisibility(View.GONE);
        textView_help_8.setText("");
//        loadImageView(imageView_help_8, language + "/"+item+"/add_user_8.png");

    }



    private void loadImageView(final ImageView imageView, String imageRef) {
        imageView.setVisibility(View.GONE);

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://personal-gps-tracker.appspot.com/");
        StorageReference islandRef = storageRef.child("help/en/" + imageRef);

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                imageView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                imageView.setVisibility(View.GONE);
            }
        });
    }

    private void setupHelp_how_to_accept_invitations() {
        textView_help_title.setVisibility(View.VISIBLE);
        textView_help_title.setText(getString(R.string.how_to_accept_invitations));

        textView_help_1.setVisibility(View.VISIBLE);
        textView_help_1.setText(getString(R.string.how_to_accept_invitations_1));
//        loadImageView(imageView_help_1, language + "/" + item + "/add_place_1.png");

        textView_help_2.setVisibility(View.VISIBLE);
        textView_help_2.setText(getString(R.string.how_to_accept_invitations_2));
//        loadImageView(imageView_help_2, language + "/" + item + "/add_place_2.png");

        textView_help_3.setVisibility(View.VISIBLE);
        textView_help_3.setText(getString(R.string.how_to_accept_invitations_3));
//        loadImageView(imageView_help_3, language + "/" + item + "/add_place_3.png");

        textView_help_4.setVisibility(View.VISIBLE);
        textView_help_4.setText(getString(R.string.how_to_accept_invitations_4));
//        loadImageView(imageView_help_4, language + "/" + item + "/add_place_4.png");

        textView_help_5.setVisibility(View.VISIBLE);
        textView_help_5.setText(getString(R.string.how_to_accept_invitations_5));
//        loadImageView(imageView_help_5, language + "/" + item + "/add_place_5.png");

        textView_help_6.setVisibility(View.GONE);
//        textView_help_6.setText("");
//        loadImageView(imageView_help_6, language + "/" + item + "/add_place_6.png");

        textView_help_7.setVisibility(View.GONE);
        textView_help_7.setText("");
//        loadImageView(imageView_help_7, language + "/"+item+"/add_user_7.png");

        textView_help_8.setVisibility(View.GONE);
        textView_help_8.setText("");
//        loadImageView(imageView_help_8, language + "/"+item+"/add_user_8.png");

    }

    private void setupHelp_how_to_add_places_on_the_map() {
        textView_help_title.setVisibility(View.VISIBLE);
        textView_help_title.setText(getString(R.string.how_to_add_places_on_the_map));

        textView_help_1.setVisibility(View.VISIBLE);
        textView_help_1.setText(getString(R.string.how_to_add_places_on_the_map_1));
        loadImageView(imageView_help_1, item + "/add_place_1.png");

        textView_help_2.setVisibility(View.VISIBLE);
        textView_help_2.setText(getString(R.string.how_to_add_places_on_the_map_2));
        loadImageView(imageView_help_2, item + "/add_place_2.png");

        textView_help_3.setVisibility(View.VISIBLE);
        textView_help_3.setText(getString(R.string.how_to_add_places_on_the_map_3));
        loadImageView(imageView_help_3, item + "/add_place_3.png");

        textView_help_4.setVisibility(View.VISIBLE);
        textView_help_4.setText(getString(R.string.how_to_add_places_on_the_map_4));
        loadImageView(imageView_help_4, item + "/add_place_4.png");

        textView_help_5.setVisibility(View.VISIBLE);
        textView_help_5.setText(getString(R.string.how_to_add_places_on_the_map_5));
        loadImageView(imageView_help_5, item + "/add_place_5.png");

        textView_help_6.setVisibility(View.VISIBLE);
        textView_help_6.setText(getString(R.string.how_to_add_places_on_the_map_6));
        loadImageView(imageView_help_6, item + "/add_place_6.png");

        textView_help_7.setVisibility(View.GONE);
        textView_help_7.setText("");
//        loadImageView(imageView_help_7, language + "/"+item+"/add_user_7.png");

        textView_help_8.setVisibility(View.GONE);
        textView_help_8.setText("");
//        loadImageView(imageView_help_8, language + "/"+item+"/add_user_8.png");

    }

    private void setupHelp_why_my_contacts_dont_move() {
        textView_help_title.setVisibility(View.VISIBLE);
        textView_help_title.setText(getString(R.string.icon_does_not_update_1));

        textView_help_1.setVisibility(View.VISIBLE);
        textView_help_1.setText(getString(R.string.icon_does_not_update_2));
//        loadImageView(imageView_help_1, language + "/" + item + "/add_place_1.png");

        textView_help_2.setVisibility(View.VISIBLE);
        textView_help_2.setText(getString(R.string.icon_does_not_update_3));
//        loadImageView(imageView_help_2, language + "/" + item + "/add_place_2.png");

        textView_help_3.setVisibility(View.VISIBLE);
        textView_help_3.setText(getString(R.string.icon_does_not_update_4));
//        loadImageView(imageView_help_3, language + "/" + item + "/add_place_3.png");

        textView_help_4.setVisibility(View.VISIBLE);
        textView_help_4.setText(getString(R.string.icon_does_not_update_5));
//        loadImageView(imageView_help_4, language + "/" + item + "/add_place_4.png");

        textView_help_5.setVisibility(View.VISIBLE);
        textView_help_5.setText(getString(R.string.icon_does_not_update_6));
//        loadImageView(imageView_help_5, language + "/" + item + "/add_place_5.png");

        textView_help_6.setVisibility(View.GONE);
//        textView_help_6.setText("");
//        loadImageView(imageView_help_6, language + "/" + item + "/add_place_6.png");

        textView_help_7.setVisibility(View.GONE);
        textView_help_7.setText("");
//        loadImageView(imageView_help_7, language + "/"+item+"/add_user_7.png");

        textView_help_8.setVisibility(View.GONE);
        textView_help_8.setText("");
//        loadImageView(imageView_help_8, language + "/"+item+"/add_user_8.png");


    }

    private void setupHelp_why_the_location_is_not_accurate() {
        textView_help_title.setVisibility(View.VISIBLE);
        textView_help_title.setText(getString(R.string.icon_does_not_update_1));

        textView_help_1.setVisibility(View.VISIBLE);
        textView_help_1.setText(getString(R.string.icon_does_not_update_2));
//        loadImageView(imageView_help_1, language + "/" + item + "/add_place_1.png");

        textView_help_2.setVisibility(View.VISIBLE);
        textView_help_2.setText(getString(R.string.icon_does_not_update_3));
//        loadImageView(imageView_help_2, language + "/" + item + "/add_place_2.png");

        textView_help_3.setVisibility(View.VISIBLE);
        textView_help_3.setText(getString(R.string.icon_does_not_update_4));
//        loadImageView(imageView_help_3, language + "/" + item + "/add_place_3.png");

        textView_help_4.setVisibility(View.VISIBLE);
        textView_help_4.setText(getString(R.string.icon_does_not_update_5));
//        loadImageView(imageView_help_4, language + "/" + item + "/add_place_4.png");

        textView_help_5.setVisibility(View.VISIBLE);
        textView_help_5.setText(getString(R.string.icon_does_not_update_6));
//        loadImageView(imageView_help_5, language + "/" + item + "/add_place_5.png");

        textView_help_6.setVisibility(View.GONE);
//        textView_help_6.setText("");
//        loadImageView(imageView_help_6, language + "/" + item + "/add_place_6.png");

        textView_help_7.setVisibility(View.GONE);
        textView_help_7.setText("");
//        loadImageView(imageView_help_7, language + "/"+item+"/add_user_7.png");

        textView_help_8.setVisibility(View.GONE);
        textView_help_8.setText("");
//        loadImageView(imageView_help_8, language + "/"+item+"/add_user_8.png");

    }


}
