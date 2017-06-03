package com.sow.gpstrackerpro.classes;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sow.gpstrackerpro.application.MyApplication;

import java.util.List;

public class ActivityRecognizedService extends IntentService {

    private static final String TAG = "ActivityRecognizedService";
    private MyApplication myApplication;

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
        Log.w(TAG, "ActivityRecognizedService");
    }

    protected void onHandleIntent(Intent intent) {
        Log.w(TAG, "onHandleIntent");
        try {
            myApplication = (MyApplication) getApplicationContext();
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                handleDetectedActivities(result.getProbableActivities());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        Log.w(TAG, "handleDetectedActivities");
        try {
            String userActivity = "";
            int confidence = 75;
            for (DetectedActivity activity : probableActivities) {
                switch (activity.getType()) {
                    case DetectedActivity.IN_VEHICLE: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "In Vehicle: " + activity.getConfidence());
                            userActivity = "In Vehicle";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.ON_BICYCLE: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "On Bicycle: " + activity.getConfidence());
                            userActivity = "On Bicycle";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.ON_FOOT: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "On Foot: " + activity.getConfidence());
                            userActivity = "On Foot";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.RUNNING: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "Running: " + activity.getConfidence());
                            userActivity = "Running";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.STILL: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "Still: " + activity.getConfidence());
                            userActivity = "Still";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.TILTING: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "Tilting: " + activity.getConfidence());
                            userActivity = "Tilting";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.WALKING: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "Walking: " + activity.getConfidence());
                            userActivity = "Walking";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.UNKNOWN: {
                        if (activity.getConfidence() >= confidence) {
                            Log.i(TAG, "Unknown: " + activity.getConfidence());
                            userActivity = "Unknown";
                            confidence = activity.getConfidence();
                        }
                        break;
                    }

                }
            }
            myApplication.getEventActivityDetected().doEvent(userActivity, confidence);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}