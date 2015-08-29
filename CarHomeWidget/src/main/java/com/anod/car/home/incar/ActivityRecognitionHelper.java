package com.anod.car.home.incar;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.content.Intent;

/**
 * @author alex
 * @date 6/7/13
 */
public class ActivityRecognitionHelper {

    public static boolean checkCarState(Intent intent, boolean defaultState) {
        ActivityRecognitionResult result = (ActivityRecognitionResult) intent.getExtras()
                .get(ModeBroadcastReceiver.EXTRA_ACTIVITY_RESULT);
        DetectedActivity probActivity = result.getMostProbableActivity();
        int type = probActivity.getType();
        if (type == DetectedActivity.IN_VEHICLE) {
            return true;
        }
        if (type == DetectedActivity.ON_FOOT || type == DetectedActivity.WALKING) {
            return false;
        }
        return defaultState;
    }

    public static boolean typeSupported(int type) {
        return type == DetectedActivity.ON_FOOT || type == DetectedActivity.IN_VEHICLE
                || type == DetectedActivity.WALKING;
    }
}
