package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

/**
 * Created by renato on 26/02/2017.
 */

@Keep
public class CheckIn {

    String user;
    String userName;
    String action;
    String place;
    Long time;

    public CheckIn() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
