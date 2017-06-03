package com.sow.gpstrackerpro.events;

public class EventMarkersOnMapListReady {

    private OnMarkersOnMapListReady onMarkersOnMapListReady;

    public void setOnEventListener(OnMarkersOnMapListReady listener) {
        this.onMarkersOnMapListReady = listener;
    }

    public void doEvent() {
        if (onMarkersOnMapListReady != null)
            onMarkersOnMapListReady.onEvent(); // event object :)
    }
}