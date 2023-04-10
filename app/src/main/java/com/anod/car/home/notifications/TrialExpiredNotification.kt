package com.anod.car.home.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object TrialExpiredNotification {
    private const val id = 2

    fun show(context: Context) {
        val r = context.resources
        val notifText = r.getString(info.anodsplace.carwidget.content.R.string.notif_consider)
        val notification = NotificationCompat.Builder(context, "general")
                .setSmallIcon(info.anodsplace.carwidget.skin.R.drawable.ic_stat_incar)
                .setAutoCancel(true)
                .setContentTitle(r.getString(info.anodsplace.carwidget.content.R.string.notif_expired))
                .setTicker(notifText)
                .setContentTitle(notifText)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_IMMUTABLE))
                .build()

        val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
        notificationManager.cancel(id)
    }
}