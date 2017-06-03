package com.sow.gpstrackerpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class PurchaseCompletedActivity extends AppCompatActivity {

    private Toolbar toolbar;
    Button button_activate_premium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_completed);


        toolbar = (Toolbar) findViewById(R.id.toolbar_purchase_completed);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        button_activate_premium = (Button) findViewById(R.id.button_activate_premium);
        button_activate_premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PurchaseCompletedActivity.this, MainActivity.class));
            }
        });

    }
}
