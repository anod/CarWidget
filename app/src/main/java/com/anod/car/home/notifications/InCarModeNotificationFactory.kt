package com.anod.car.home.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.anod.car.home.R
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.incar.ModeService
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.shortcuts.NotificationShortcutsModel
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources

class InCarModeNotificationFactory(
        private val context: Context,
        private val database: ShortcutsDatabase,
        private val iconLoader: ShortcutIconLoader,
        private val shortcutResources: ShortcutResources
) {
    companion object {
        const val id = 1
        private val buttonIds = intArrayOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3)
    }

    suspend fun create(): Notification {
        val notificationIntent = ModeService.createStartIntent(context, ModeService.MODE_SWITCH_OFF)
        notificationIntent.data = Deeplink.SwitchMode(false).toUri()

        val r = context.resources
        val contentIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, Channels.inCarMode)
                .setSmallIcon(info.anodsplace.carwidget.skin.R.drawable.ic_stat_incar)
                .setOngoing(true)
                .addAction(0, context.getString(info.anodsplace.carwidget.content.R.string.disable), contentIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val model = NotificationShortcutsModel.init(context, database)
        if (model.filledCount > 0) {
            val contentView = createShortcuts(context, model, iconLoader)
            contentView.setTextViewText(android.R.id.text1, "")
            notification.setContent(contentView)
        } else {
            notification.setContentTitle(r.getString(info.anodsplace.carwidget.content.R.string.incar_mode_enabled))
        }

        return notification.build()
    }

    private suspend fun createShortcuts(context: Context, model: NotificationShortcutsModel, iconLoader: ShortcutIconLoader): RemoteViews {
        val contentView = RemoteViews(context.packageName, R.layout.notification)

        val spi = ShortcutPendingIntent(context, shortcutResources)
        for (position in 0 until model.count) {
            val info = model.get(position)
            val resId = buttonIds[position]
            if (info == null) {
                contentView.setViewVisibility(resId, View.GONE)
            } else {
                val icon = iconLoader.load(info, "")
                contentView.setImageViewBitmap(resId, icon.bitmap)
                spi.createShortcut(info.intent, uri = { Deeplink.OpenNotificationShortcut(position).toUri() })?.let {
                    contentView.setOnClickPendingIntent(resId, it)
                }
            }
        }

        return contentView
    }

}