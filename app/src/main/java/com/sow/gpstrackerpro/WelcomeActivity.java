package com.sow.gpstrackerpro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;

import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Log;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private MyApplication myApplication;
    private Animation animFadein;
    private ImageView imageView_grampa, imageView_son, imageView_daugher;

    private GoogleApiClient mClient;
    private String mUrl;
    private String mTitle;
    private String mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        myApplication = (MyApplication) getApplicationContext();

        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        mUrl = "http://family-locator.net/welcome";
        mTitle = "Welcome";
        mDescription = "Welcome page";

        if(myApplication.getFirebaseUser() == null) {

            viewPager = (ViewPager) findViewById(R.id.view_pager);
            dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
            btnSkip = (Button) findViewById(R.id.btn_skip);
            btnNext = (Button) findViewById(R.id.btn_next);

            // layouts of all welcome sliders
            // add few more layouts if you want
            layouts = new int[]{
                    R.layout.tutorial_location_permission_request_1,
                    R.layout.tutorial_location_permission_request_2,
                    R.layout.welcome_slide_3};


            addBottomDots(0);

            // making notification bar transparent
            //        changeStatusBarColor();

            myViewPagerAdapter = new MyViewPagerAdapter();
            viewPager.setAdapter(myViewPagerAdapter);
            viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchHomeScreen();
                }
            });

            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // checking for last page
                    // if last page home screen will be launched
                    int current = getItem(+1);
                    if (current < layouts.length) {
                        // move to next screen
                        viewPager.setCurrentItem(current);
                    } else {
                        launchHomeScreen();
                    }
                }
            });
        } else {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
        mClient.connect();
    }

    public Action getAction() {
        Thing object = (Thing) new Thing.Builder()
                .setName(mTitle)
                .setDescription(mDescription)
                .setUrl(Uri.parse(mUrl))
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            Log.w(TAG, "onNewIntent");
        }
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.letsgo));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            if (position == 0) {
                TextView textView_1_slide_1 = (TextView) view.findViewById(R.id.textView_1_slide_1);
                Typeface typeFace = Typeface.createFromAsset(getAssets(), "light.ttf");
                textView_1_slide_1.setTypeface(typeFace);
                textView_1_slide_1.setText(getString(R.string.textView_1_slide_1));

                TextView textView_2_slide_1 = (TextView) view.findViewById(R.id.textView_2_slide_1);
                textView_2_slide_1.setText(getString(R.string.textView_2_slide_1));

            } else if (position == 1) {
                TextView textView_1_slide_2 = (TextView) view.findViewById(R.id.textView_1_slide_2);
                Typeface typeFace = Typeface.createFromAsset(getAssets(), "light.ttf");
                textView_1_slide_2.setTypeface(typeFace);
                textView_1_slide_2.setText(getString(R.string.textView_1_slide_2));

                TextView textView_2_slide_2 = (TextView) view.findViewById(R.id.textView_2_slide_2);
                textView_2_slide_2.setText(getString(R.string.textView_2_slide_2));

            } else if (position == 2) {
                TextView textView_1_slide_3 = (TextView) view.findViewById(R.id.textView_1_slide_3);
                Typeface typeFace = Typeface.createFromAsset(getAssets(), "light.ttf");
                textView_1_slide_3.setTypeface(typeFace);
                textView_1_slide_3.setText(getString(R.string.textView_1_slide_3));

                TextView textView_2_slide_3 = (TextView) view.findViewById(R.id.textView_2_slide_3);
                textView_2_slide_3.setText(getString(R.string.textView_2_slide_3));

            }
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
