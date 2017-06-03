package com.sow.gpstrackerpro.events;

public class EventActivityDetected {

    private OnActivityDetected mOnActivityDetected;

    public void setOnEventListener(OnActivityDetected listener) {
        this.mOnActivityDetected = listener;
    }

    public void doEvent(String userActivity, int confidence) {
        if (mOnActivityDetected != null)
            mOnActivityDetected.onEvent(userActivity, confidence); // event object :)
    }
}