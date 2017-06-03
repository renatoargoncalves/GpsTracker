package com.sow.gpstrackerpro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        toolbar = (Toolbar) findViewById(R.id.toolbar_terms_and_conditions);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.terms_and_conditions_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }
}
