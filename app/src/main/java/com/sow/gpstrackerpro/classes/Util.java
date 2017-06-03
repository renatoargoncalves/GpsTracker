package com.sow.gpstrackerpro.classes;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sow.gpstrackerpro.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    private static final String TAG = "Util";

    public static boolean isDataConnected(Context context) {
        Log.i(TAG, "isDataConnected()");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isGpsEnabled(Context context) {
        Log.i(TAG, "isGpsEnabled()");
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String getHash64(Activity activity, String pkg) {
        Log.i(TAG, "getHash64()");
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(pkg,
                    PackageManager.GET_SIGNATURES);
            String hash = new String("");
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d(TAG, "Hash64: " + hash);
            }
            return hash;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Hash64: " + e.getLocalizedMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Hash64: " + e.getLocalizedMessage());
            return null;
        }

    }

    public static String getAppVersionName(Context context) {
        Log.i(TAG, "getAppVersionName()");
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;
    }

    public static int getAppVersionCode(Context context) {
        Log.i(TAG, "getAppVersionNumber()");
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionCode;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static int pxToDp(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

}
