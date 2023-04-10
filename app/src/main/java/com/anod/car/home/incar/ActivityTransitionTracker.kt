package com.anod.car.home.incar

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import info.anodsplace.applog.AppLog

class ActivityTransitionTracker(private val context: Context) {

    private val broadcastIntent = PendingIntent.getBroadcast(
            context, 0 ,
            Intent(context, ModeBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)!!

    fun track() {
        val transitions = listOf(
            activityTransitionOf(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            activityTransitionOf(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
            activityTransitionOf(DetectedActivity.ON_FOOT, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            activityTransitionOf(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        )

        val request = ActivityTransitionRequest(transitions)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            AppLog.e("Activity recognition permission is not granted")
            return
        }

        val task = ActivityRecognition.getClient(context).requestActivityTransitionUpdates(request, broadcastIntent)
        task.addOnSuccessListener {
            AppLog.i("Activity is tracking")
        }

        task.addOnFailureListener {
            AppLog.e(it)
        }
    }

    fun stop() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            AppLog.e("Activity recognition permission is not granted")
            return
        }

        ActivityRecognition.getClient(context).removeActivityTransitionUpdates(broadcastIntent)
    }

    companion object {
        fun checkCarState(result: ActivityTransitionResult): Boolean {
            AppLog.i("Events:  " + result.transitionEvents.joinToString { "; " })
            val event = result.transitionEvents.lastOrNull() ?: return  false

            return when(event.activityType) {
                DetectedActivity.IN_VEHICLE -> event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                else -> false
            }
        }
    }
}

private fun activityTransitionOf(type: Int, transition: Int) =
        ActivityTransition.Builder()
                .setActivityTransition(transition)
                .setActivityType(type)
                .build()