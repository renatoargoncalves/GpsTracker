package com.sow.gpstrackerpro.events;

public interface OnActivityDetected {
    void onEvent(String userActivity, int confidence);
}