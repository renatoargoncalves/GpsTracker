package com.sow.gpstrackerpro.events;

public class EventPremiumReceived {

    private OnPremiumReceived mOnPremiumReceived;

    public void setOnEventListener(OnPremiumReceived listener) {
        this.mOnPremiumReceived= listener;
    }

    public void doEvent(boolean premium) {
        if (mOnPremiumReceived!= null)
            mOnPremiumReceived.onEvent(premium); // event object :)
    }
}