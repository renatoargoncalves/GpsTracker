package com.sow.gpstrackerpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.sow.gpstrackerpro.classes.Log;

public class HelpActivity extends AppCompatActivity {

    private static final String TAG = "HelpActivity";
    private Toolbar toolbar;
    private ImageView imageView_how_to_invite_user_1, imageView_how_to_invite_user_2, imageView_how_to_invite_user_3;
    private ListView listView_help;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Configures the Toolbar on the MainActivity and sets the title
        toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        final String[] topics = {
                getString(R.string.how_it_works),
                getString(R.string.how_to_add_users_on_the_map),
                getString(R.string.how_to_accept_invitations),
                getString(R.string.how_to_add_places_on_the_map),
                getString(R.string.icon_does_not_update_1),
                getString(R.string.why_precision_is_bad_1)
        };

        listView_help = (ListView) findViewById(R.id.listView_help);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, topics);
        listView_help.setAdapter(adapter);
        listView_help.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(HelpActivity.this, HelpDetails.class);
                Bundle b = new Bundle();
                b.putInt("item", i);
                intent.putExtras(b);
                startActivity(intent);
                Log.i(TAG, "sent: "+i);
            }
        });

//        imageView_how_to_invite_user_1 = (ImageView) findViewById(R.id.imageView_how_to_invite_user_1);
//        imageView_how_to_invite_user_2 = (ImageView) findViewById(R.id.imageView_how_to_invite_user_2);
//        imageView_how_to_invite_user_3 = (ImageView) findViewById(R.id.imageView_how_to_invite_user_3);
//
//        if( Locale.getDefault().getDisplayLanguage().equals(Locale.ENGLISH)) {
//            imageView_how_to_invite_user_1.setImageResource(R.drawable.how_to_add_user_en);
//            imageView_how_to_invite_user_2.setImageResource(R.drawable.how_to_send_invite_1_en);
//            imageView_how_to_invite_user_3.setImageResource(R.drawable.how_to_send_invite_2_en);
//        } else if (Locale.getDefault().getDisplayLanguage().contains("portugu")) {
//            imageView_how_to_invite_user_1.setImageResource(R.drawable.how_to_add_user_pt);
//            imageView_how_to_invite_user_2.setImageResource(R.drawable.how_to_send_invite_1_pt);
//            imageView_how_to_invite_user_3.setImageResource(R.drawable.how_to_send_invite_2_pt);
//        }

    }
}
