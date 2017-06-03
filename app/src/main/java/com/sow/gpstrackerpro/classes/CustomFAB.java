package com.sow.gpstrackerpro.classes;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.util.AttributeSet;

import com.sow.gpstrackerpro.R;

/**
 * Created by renato on 27/04/2017.
 */

public class CustomFAB extends android.support.design.widget.FloatingActionButton {

    private AnimatedVectorDrawable state1;
    private AnimatedVectorDrawable state2;
    private boolean mIsShowingState2;

    public CustomFAB(Context context) {
        super(context);
        init();
    }

    public CustomFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mIsShowingState2 = false;
            setImageDrawable(state1);
        }
    }

    public void morph() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            state1 = (AnimatedVectorDrawable) getContext().;
//            state2 = (AnimatedVectorDrawable) getContext().;

            final AnimatedVectorDrawable drawable = mIsShowingState2 ? state2 : state1;
            drawable.start();
            mIsShowingState2 = !mIsShowingState2;
            if(mIsShowingState2) {
                setImageDrawable(state2);
            } else {
                setImageDrawable(state1);
            }
        }
    }

    public boolean ismIsShowingState2() {
        return mIsShowingState2;
    }

    public void setmIsShowingState2(boolean mIsShowingState2) {
        this.mIsShowingState2 = mIsShowingState2;
    }

    public AnimatedVectorDrawable getState1() {
        return state1;
    }

    public void setState1(AnimatedVectorDrawable state1) {
        this.state1 = state1;
    }

    public AnimatedVectorDrawable getState2() {
        return state2;
    }

    public void setState2(AnimatedVectorDrawable state2) {
        this.state2 = state2;
    }
}
