package com.anod.car.home.incar

import com.anod.car.home.R
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

import com.anod.car.home.BuildConfig
import info.anodsplace.framework.AppLog

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat

/**
 * @author alex
 * @date 6/3/13
 */
class ActivityRecognitionService : IntentService("ActivityRecognitionService") {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onHandleIntent(intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)

            if (BuildConfig.DEBUG) {
                val probActivity = result.mostProbableActivity
                AppLog.d("Activity: [" + String.format("%03d", probActivity.confidence) + "] "
                        + renderActivityType(probActivity.type))

                val noti = NotificationCompat.Builder(this)
                        .setContentTitle("Activity")
                        .setContentText("[" + String.format("%03d", probActivity.confidence) + "] " + renderActivityType(probActivity.type))
                        .setSmallIcon(R.drawable.ic_launcher_application)
                        .setTicker("[" + String.format("%03d", probActivity.confidence) + "] " + renderActivityType(probActivity.type))
                        .build()
                val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(122, noti)
            }

            val probActivity = result.mostProbableActivity
            val conf = probActivity.confidence
            if (probActivity.confidence < MIN_CONFIDENCE) {
                return
            }
            val type = probActivity.type
            if (type == DetectedActivity.IN_VEHICLE && conf < MIN_VEHICLE_CONFIDENCE) {
                return
            }
            if (ActivityRecognitionHelper.typeSupported(type)) {
                synchronized(sLock) {
                    if (sLastResult != type) {
                        sLastResult = type
                        val broadcast = Intent(
                                ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION)

                        broadcast.putExtra(ModeBroadcastReceiver.EXTRA_ACTIVITY_RESULT, result)
                        sendBroadcast(broadcast)
                    }
                }
            }

        } else {
            AppLog.d("ActivityRecognitionResult: No Result")
        }
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

    companion object {

        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Any()

        const val MIN_CONFIDENCE = 40
        const val MIN_VEHICLE_CONFIDENCE = 75

        private var sLastResult = -1

        fun resetLastResult() {
            synchronized(sLock) {
                sLastResult = -1
            }
        }
    }
}