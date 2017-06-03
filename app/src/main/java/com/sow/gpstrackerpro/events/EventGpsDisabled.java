package com.sow.gpstrackerpro.events;

public class EventGpsDisabled {

    private OnGpsDisabled mOnGpsDisabled;

    public void setOnEventListener(OnGpsDisabled listener) {
        this.mOnGpsDisabled = listener;
    }

    public void doEvent() {
        if (mOnGpsDisabled != null)
            mOnGpsDisabled.onEvent(); // event object :)
    }
}