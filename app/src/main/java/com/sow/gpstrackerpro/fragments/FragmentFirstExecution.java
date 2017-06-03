package com.sow.gpstrackerpro.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sow.gpstrackerpro.R;
import com.sow.gpstrackerpro.classes.Fragments;

public class FragmentFirstExecution extends Fragment {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private OnFragmentFirstExecutionFinishes onFragmentFirstExecutionFinishes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_execution, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) view.findViewById(R.id.layoutDots);
        btnSkip = (Button) view.findViewById(R.id.btn_skip);
        btnNext = (Button) view.findViewById(R.id.btn_next);

        layouts = new int[]{
                R.layout.tutorial_location_permission_request_1,
                R.layout.tutorial_location_permission_request_2};

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentFirstExecutionFinishes.onFragmentFirstExecutionFinishes(Fragments.FIRST_EXECUTION);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(+1);
                if (current < layouts.length) {
                    viewPager.setCurrentItem(current);
                } else {
                    onFragmentFirstExecutionFinishes.onFragmentFirstExecutionFinishes(Fragments.FIRST_EXECUTION);
                }
            }
        });

        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        return view;
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(getActivity());
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

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

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            if (position == 0) {
                TextView textView_1_slide_1 = (TextView) view.findViewById(R.id.textView_1_slide_1);
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "light.ttf");
                textView_1_slide_1.setTypeface(typeFace);

            } else if (position == 1) {
                TextView textView_1_slide_2 = (TextView) view.findViewById(R.id.textView_1_slide_2);
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "light.ttf");
                textView_1_slide_2.setTypeface(typeFace);

            } else if (position == 2) {
                TextView textView_1_slide_3 = (TextView) view.findViewById(R.id.textView_1_slide_3);
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "light.ttf");
                textView_1_slide_3.setTypeface(typeFace);

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

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    public interface OnFragmentFirstExecutionFinishes {
        public void onFragmentFirstExecutionFinishes(Fragments fragmentName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onFragmentFirstExecutionFinishes = (OnFragmentFirstExecutionFinishes) context;
    }
}