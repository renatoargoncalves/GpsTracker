package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
public class User {

    public String uid;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
