package com.anod.car.home.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.anod.car.home.R
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import info.anodsplace.framework.AppLog

object ActivityRecognitionNotification {
    private const val notificationId = 4

    fun show(result: ActivityRecognitionResult, context: Context) {
        val probActivity = result.mostProbableActivity
        AppLog.d("Activity: [" + String.format("%03d", probActivity.confidence) + "] "
                + renderActivityType(probActivity.type))

        val notification = NotificationCompat.Builder(context, Channels.general)
                .setContentTitle("Activity")
                .setContentText("[" + String.format("%03d", probActivity.confidence) + "] " + renderActivityType(probActivity.type))
                .setSmallIcon(R.drawable.ic_launcher_application)
                .setTicker("[" + String.format("%03d", probActivity.confidence) + "] " + renderActivityType(probActivity.type))
                .build()
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notificationId, notification)
    }

    private fun renderActivityType(type: Int): String {
        if (type == DetectedActivity.IN_VEHICLE) {
            return "IN_VEHICLE"
        }
        if (type == DetectedActivity.ON_BICYCLE) {
            return "ON_BICYCLE"
        }
        if (type == DetectedActivity.ON_FOOT) {
            return "ON_FOOT"
        }
        if (type == DetectedActivity.STILL) {
            return "STILL (NOT MOOVING)"
        }
        return if (type == DetectedActivity.TILTING) {
            "TILTING"
        } else "UNKNOWN ($type)"
    }
}