package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

@Keep
public class UserLocation {


    public double latitude;
    public double longitude;
    public double accuracy;
    public String lastUpdate;
    public float speed;
    public String state;
    public String confidence;
    public float battery;

    public UserLocation() {
    }

    public UserLocation(double latitude, double longitude, double accuracy, String lastUpdate, float speed, String state, String confidence, float battery) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.lastUpdate = lastUpdate;
        this.speed = speed;
        this.state = state;
        this.confidence = confidence;
        this.battery = battery;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }
}
