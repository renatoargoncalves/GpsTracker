package com.sow.gpstrackerpro.classes;

import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by renato on 19/12/2016.
 */

@Keep
public class Invite {

    public String user;
    public long inv_date;

    public Invite(String user, long inv_date) {
        this.user = user;
        this.inv_date = inv_date;
    }

    public Invite() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getInv_date() {
        return inv_date;
    }

    public void setInv_date(long inv_date) {
        this.inv_date = inv_date;
    }
}
