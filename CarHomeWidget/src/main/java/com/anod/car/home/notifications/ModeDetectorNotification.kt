package com.anod.car.home.notifications

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.anod.car.home.R

object ModeDetectorNotification {
    const val id = 3
    fun create(context: Context): Notification {
        return NotificationCompat.Builder(context, Channels.modeDetector)
                .setContentText(context.getString(R.string.notifcation_mode_detector))
                .setSmallIcon(R.drawable.ic_stat_mode_detector)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
    }
}