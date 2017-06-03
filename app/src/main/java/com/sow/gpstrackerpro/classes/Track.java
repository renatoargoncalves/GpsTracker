package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

@Keep
public class Track {


    public String user;
    public boolean permission;

    public Track() {
    }

    public Track(String user, boolean permission) {
        this.user = user;
        this.permission = permission;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }
}
