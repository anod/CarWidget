package com.anod.car.home.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.anod.car.home.R

object Channels {
    const val modeDetector: String = "mode_detector"
    const val inCarMode: String = "incar_mode"
    const val general: String = "general"

    fun register(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(modeDetector, context.getString(R.string.mode_detector_channel), NotificationManager.IMPORTANCE_LOW)
            val channel2 = NotificationChannel("incar_mode", context.getString(R.string.incar_mode), NotificationManager.IMPORTANCE_DEFAULT)
            val channel3 = NotificationChannel(context.getString(R.string.channel_crash_reports), "Crash reports", NotificationManager.IMPORTANCE_HIGH)
            val channel4 = NotificationChannel("general", context.getString(R.string.general), NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = context.getSystemService(NotificationManager::class.java)!!
            notificationManager.createNotificationChannels(listOf(channel1, channel2, channel3, channel4))
        }
    }
}