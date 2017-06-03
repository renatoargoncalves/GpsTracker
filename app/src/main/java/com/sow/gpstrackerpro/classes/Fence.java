package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by renato on 21/02/2017.
 */

@Keep
public class Fence {

    float radius;
    double lat;
    double lng;
    String name;
    String owner;
    boolean showOnMap = false;
    boolean showNotification = false;

    public Fence() {
    }

    public Fence(float radius, double lat, double lng, String name, String owner, boolean showOnMap, boolean showNotification) {
        this.radius = radius;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.owner = owner;
        this.showOnMap = showOnMap;
        this.showNotification = showNotification;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    public void setShowOnMap(boolean showOnMap) {
        this.showOnMap = showOnMap;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }
}
