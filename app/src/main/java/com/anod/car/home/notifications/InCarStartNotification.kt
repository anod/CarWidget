package com.anod.car.home.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.anod.car.home.MainActivity
import com.anod.car.home.incar.ModeService
import com.anod.car.home.incar.SwitchInCarActivity
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.R

internal object InCarStartNotification {
    private const val activationId = 4
    private const val detectorId = 5

    fun show(context: Context, activate: Boolean, forceState: Boolean = false) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled() ||
            (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED)
        ) {
            AppLog.w("Cannot show in-car service recovery: notifications are disabled")
            return
        }

        val intent = if (activate) {
            Intent(context, SwitchInCarActivity::class.java)
                .putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON)
                .putExtra(ModeService.EXTRA_FORCE_STATE, forceState)
        } else {
            Intent(context, MainActivity::class.java)
        }
        val id = if (activate) activationId else detectorId
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, Channels.inCarMode)
            .setSmallIcon(info.anodsplace.carwidget.skin.R.drawable.ic_stat_incar)
            .setContentTitle(context.getString(R.string.incar_mode))
            .setContentText(context.getString(R.string.incar_service_start_blocked))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.incar_service_start_blocked)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        try {
            manager.notify(id, notification)
        } catch (e: SecurityException) {
            AppLog.e(e)
        }
    }

    fun cancel(context: Context, activate: Boolean) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(if (activate) activationId else detectorId)
    }
}
