package com.anod.car.home.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.anod.car.home.MainActivity

object ModeDetectorNotification {
    const val id = 3
    fun create(context: Context): Notification {
        return NotificationCompat.Builder(context, Channels.modeDetector)
                .setContentTitle(context.getString(info.anodsplace.carwidget.content.R.string.notification_mode_detector_title))
                .setContentText(context.getString(info.anodsplace.carwidget.content.R.string.notification_mode_detector_content))
                .setSmallIcon(info.anodsplace.carwidget.skin.R.drawable.ic_stat_mode_detector)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
                    // TODO: Handle URI
                    data = Uri.parse("carwidget://info.anodsplace.carwidget/incar/main")
                }, PendingIntent.FLAG_IMMUTABLE))
                .build()
    }
}