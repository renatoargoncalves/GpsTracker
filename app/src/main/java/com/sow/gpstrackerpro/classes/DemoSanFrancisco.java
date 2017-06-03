package com.sow.gpstrackerpro.classes;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by renato on 04/03/2017.
 */

public class DemoSanFrancisco {

    public static ArrayList<UserLocation> getLeoPositions() {
        ArrayList<UserLocation> leoList = new ArrayList<>();

        Calendar now = Calendar.getInstance();
        Long tsLong = now.getTimeInMillis();

        UserLocation schooSanFrancisco = new UserLocation(37.774857759967624, -122.41635337471962, 20, tsLong.toString(), 0, "On Bicycle", "77", 0.43f);
        UserLocation leoAtSchoolSanFrancisco = new UserLocation(37.774808759967624, -122.41545337471962, 20, tsLong.toString(), 0, "Still", "88", 0.73f);
        UserLocation userLocation4 = new UserLocation(34.050064, -118.311081, 20, tsLong.toString(), 0, "On Bicycle", "78", 0.43f);
        UserLocation userLocation5 = new UserLocation(40.732021, -74.008756, 20, tsLong.toString(), 0, "In Vehicle", "100", 0.04f);
        UserLocation userLocation6 = new UserLocation(37.78996598d, -122.41111876d, 20, tsLong.toString(), 0, "On Bicycle", "80", 0.43f);

        leoList.add(leoAtSchoolSanFrancisco);
//        leoList.add(userLocation4);
//        leoList.add(userLocation5);
//        leoList.add(userLocation6);

        return leoList;
    }

}
