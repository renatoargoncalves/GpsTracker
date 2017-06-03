package com.sow.gpstrackerpro.classes;


import android.support.annotation.Keep;

import java.util.ArrayList;

@Keep
public class TracksContainer {

    public ArrayList<Track> tracks = new ArrayList<>();

    public TracksContainer() {
    }

    public TracksContainer(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }
}
