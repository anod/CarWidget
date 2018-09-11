package com.anod.car.home.incar

import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

import android.content.Intent

/**
 * @author alex
 * @date 6/7/13
 */
object ActivityRecognitionHelper {

    fun checkCarState(intent: Intent, defaultState: Boolean): Boolean {
        val result = intent.extras!!
                .get(ModeBroadcastReceiver.EXTRA_ACTIVITY_RESULT) as ActivityRecognitionResult
        val probActivity = result.mostProbableActivity
        val type = probActivity.type
        if (type == DetectedActivity.IN_VEHICLE) {
            return true
        }
        return if (type == DetectedActivity.ON_FOOT || type == DetectedActivity.WALKING) {
            false
        } else defaultState
    }

    fun typeSupported(type: Int): Boolean {
        return (type == DetectedActivity.ON_FOOT || type == DetectedActivity.IN_VEHICLE
                || type == DetectedActivity.WALKING)
    }
}
