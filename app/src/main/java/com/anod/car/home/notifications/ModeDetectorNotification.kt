package com.anod.car.home.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.anod.car.home.MainActivity
import com.anod.car.home.R
import com.anod.car.home.main.WidgetsListActivity

object ModeDetectorNotification {
    const val id = 3
    fun create(context: Context): Notification {
        return NotificationCompat.Builder(context, Channels.modeDetector)
                .setContentTitle(context.getString(R.string.notification_mode_detector_title))
                .setContentText(context.getString(R.string.notification_mode_detector_content))
                .setSmallIcon(R.drawable.ic_stat_mode_detector)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
                    putExtra(WidgetsListActivity.extraInCarTab, true)
                }, PendingIntent.FLAG_IMMUTABLE))
                .build()
    }
}