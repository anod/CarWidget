package com.anod.car.home.incar

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition

import com.anod.car.home.BuildConfig
import info.anodsplace.framework.AppLog

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast

/**
 * @author alex
 * @date 12/25/13
 */
class ActivityRecognitionClientService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val activityRecognitionPendingIntent: PendingIntent
        get() {
            val intent = Intent(applicationContext, ActivityRecognitionService::class.java)
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onConnected(bundle: Bundle?) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "GoogleApiClient Connected", Toast.LENGTH_SHORT).show()
            AppLog.d("GoogleApiClient: Connected")
        }

        ActivityRecognition.getClient(this).requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, activityRecognitionPendingIntent)
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this,
                    "GoogleApiClient Connection Failed: " + connectionResult.toString(),
                    Toast.LENGTH_SHORT).show()
            AppLog.d("GoogleApiClient: Failed - " + connectionResult.toString())
        }
    }

    companion object {

        // Constants used to establish the activity update interval
        private const val MILLISECONDS_PER_SECOND: Long = 1000
        private const val DETECTION_INTERVAL_SECONDS = 10
        const val DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS

        fun startService(context: Context) {
            val serviceIntent = Intent(context.applicationContext, ActivityRecognitionClientService::class.java)
            context.startService(serviceIntent)
        }

        fun stopService(context: Context) {
            val serviceIntent = Intent(context.applicationContext, ActivityRecognitionClientService::class.java)
            context.stopService(serviceIntent)
        }
    }

}
