package com.anod.car.home.incar;

import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * @author alex
 * @date 6/7/13
 */
public class ActivityRecognitionHelper {

	public static boolean checkCarState(Intent intent, boolean defaultState) {
		ActivityRecognitionResult result = (ActivityRecognitionResult)intent.getExtras().get(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT);
		DetectedActivity probActivity = result.getMostProbableActivity();
		int type = probActivity.getType();
		if (type == DetectedActivity.IN_VEHICLE) {
			return true;
		}
		if (type == DetectedActivity.ON_FOOT) {
			return false;
		}
		return defaultState;
	}

}
