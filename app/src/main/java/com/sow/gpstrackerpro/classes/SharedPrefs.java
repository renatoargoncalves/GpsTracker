package com.sow.gpstrackerpro.classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.sow.gpstrackerpro.R;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by renato on 02/05/2017.
 */

public class SharedPrefs {

    private static final String TAG = "SharedPrefs";
    private static SharedPreferences sharedPref;

    public static boolean isFirstExecutionAfterInstall(Context context) {
        Log.i(TAG, "getSharedPrefFirstExecution()");

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean result = sharedPref.getBoolean(context.getResources().getString(R.string.first_execution_after_install), true);
        return result;
    }

}
