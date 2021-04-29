package com.anod.car.home.incar

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.*
import info.anodsplace.framework.AppLog

class ActivityTransitionTracker(private val context: Context) {

    private val broadcastIntent = PendingIntent.getBroadcast(
            context, 0 ,
            Intent(context, ModeBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)!!

    fun track() {
        val transitions = listOf(
            activityTransitionOf(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            activityTransitionOf(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
            activityTransitionOf(DetectedActivity.ON_FOOT, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            activityTransitionOf(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        )

        val request = ActivityTransitionRequest(transitions)

        val task = ActivityRecognition.getClient(context).requestActivityTransitionUpdates(request, broadcastIntent)
        task.addOnSuccessListener {
            AppLog.i("Activity is tracking")
        }

        task.addOnFailureListener {
            AppLog.e(it)
        }
    }

    fun stop() {
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