package com.sow.gpstrackerpro.classes;

import android.graphics.Bitmap;
import android.support.annotation.Keep;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by renato on 18/12/2016.
 */
@Keep
public class MarkerOnMap {

    private Marker marker;
    private Marker markerInfo;

    private ValueEventListener locationEventListener;
    private ValueEventListener signInEventListener;
    private ValueEventListener powerOnEventListener;
    private ValueEventListener batteryOnEventListener;

    private UserInfo userInfo;

    private Bitmap userPicture;
    private Bitmap userPictureGray;

    private UserLocation userLocation;

    private boolean powerOn = false;


    public MarkerOnMap(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public ValueEventListener getLocationEventListener() {
        return locationEventListener;
    }

    public void setLocationEventListener(ValueEventListener locationEventListener) {
        this.locationEventListener = locationEventListener;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public ValueEventListener getSignInEventListener() {
        return signInEventListener;
    }

    public void setSignInEventListener(ValueEventListener signInEventListener) {
        this.signInEventListener = signInEventListener;
    }

    public Bitmap getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(Bitmap userPicture) {
        this.userPicture = userPicture;
    }

    public Bitmap getUserPictureGray() {
        return userPictureGray;
    }

    public void setUserPictureGray(Bitmap userPictureGray) {
        this.userPictureGray = userPictureGray;
    }

    public ValueEventListener getPowerOnEventListener() {
        return powerOnEventListener;
    }

    public void setPowerOnEventListener(ValueEventListener powerOnEventListener) {
        this.powerOnEventListener = powerOnEventListener;
    }

    public ValueEventListener getBatteryOnEventListener() {
        return batteryOnEventListener;
    }

    public void setBatteryOnEventListener(ValueEventListener batteryOnEventListener) {
        this.batteryOnEventListener = batteryOnEventListener;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public Marker getMarkerInfo() {
        return markerInfo;
    }

    public void setMarkerInfo(Marker markerInfo) {
        this.markerInfo = markerInfo;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }
}


