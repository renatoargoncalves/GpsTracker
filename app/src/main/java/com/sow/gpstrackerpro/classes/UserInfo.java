package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
public class UserInfo {

    public String displayName;
    public String icon;
    public String email;


    public UserInfo(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public UserInfo(String displayName, String icon, String email) {
        this.displayName = displayName;
        this.icon = icon;
        this.email = email;
    }

    public UserInfo() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
