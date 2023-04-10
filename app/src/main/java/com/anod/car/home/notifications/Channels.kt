package com.anod.car.home.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object Channels {
    const val modeDetector: String = "mode_detector"
    const val inCarMode: String = "incar_mode"

    fun register(context: Context) {
        val channel1 = NotificationChannel(modeDetector, context.getString(info.anodsplace.carwidget.content.R.string.mode_detector_channel), NotificationManager.IMPORTANCE_LOW)
        val channel2 = NotificationChannel("incar_mode", context.getString(info.anodsplace.carwidget.content.R.string.incar_mode), NotificationManager.IMPORTANCE_DEFAULT)
        val channel3 = NotificationChannel(context.getString(info.anodsplace.carwidget.content.R.string.channel_crash_reports), "Crash reports", NotificationManager.IMPORTANCE_HIGH)
        val channel4 = NotificationChannel("general", context.getString(info.anodsplace.carwidget.content.R.string.general), NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = context.getSystemService(NotificationManager::class.java)!!
        notificationManager.createNotificationChannels(listOf(channel1, channel2, channel3, channel4))
    }
}