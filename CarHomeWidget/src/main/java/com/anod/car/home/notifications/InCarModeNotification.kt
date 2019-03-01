package com.anod.car.home.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.anod.car.home.R
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.incar.ModeService
import com.anod.car.home.model.NotificationShortcutsModel
import com.anod.car.home.utils.Version

object InCarModeNotification {
    const val id = 1
    private const val prefix = "notif"
    private val buttonIds = intArrayOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3)

    fun create(version: Version, context: Context): Notification {
        val notificationIntent = ModeService
                .createStartIntent(context, ModeService.MODE_SWITCH_OFF)
        val data = Uri.parse("com.anod.car.home.pro://mode/0/")
        notificationIntent.data = data

        val r = context.resources
        val contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0)
        val text = if (version.isFree) {
            context.getString(R.string.click_to_disable_trial, version.trialTimesLeft)
        } else {
            r.getString(R.string.click_to_disable)
        }

        val notification = NotificationCompat.Builder(context, Channels.inCarMode)
                .setSmallIcon(R.drawable.ic_stat_incar)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val model = NotificationShortcutsModel.init(context)
        if (model.filledCount > 0) {
            val contentView = createShortcuts(context, model)
            contentView.setTextViewText(android.R.id.text1, text)
            notification.setContent(contentView)
        } else {
            notification.setContentTitle(r.getString(R.string.incar_mode_enabled))
            notification.setContentText(text)
        }

        return notification.build()
    }

    private fun createShortcuts(context: Context, model: NotificationShortcutsModel): RemoteViews {
        val contentView = RemoteViews(context.packageName, R.layout.notification)

        val spi = ShortcutPendingIntent(context)
        for (i in 0 until model.count) {
            val info = model.get(i)
            val resId = buttonIds[i]
            if (info == null) {
                contentView.setViewVisibility(resId, View.GONE)
            } else {
                val icon = model.iconLoader.load(info)
                contentView.setImageViewBitmap(resId, icon.bitmap)
                spi.createShortcut(info.intent, prefix, i)?.let {
                    contentView.setOnClickPendingIntent(resId, it)
                }
            }
        }

        return contentView
    }

}