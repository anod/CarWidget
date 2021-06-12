package com.anod.car.home.prefs.lookandfeel

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.anod.car.home.prefs.ShortcutEditActivity
import com.anod.car.home.utils.forNewShortcut
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.screens.widget.SkinList

class SkinPreviewIntentFactory(
    private val appWidgetId: Int,
    private val skinItem: SkinList.Item,
    private val context: Context
): PreviewPendingIntentFactory {

    override fun createNew(appWidgetId: Int, cellId: Int): PendingIntent {
        val intent = Intent().forNewShortcut(context, appWidgetId, cellId)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity.createIntent(appWidgetId, skinItem.value, buttonId, context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity.createIntent(appWidgetId, skinItem.value, buttonId, context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int,
                                shortcutId: Long): PendingIntent {
        val editIntent = ShortcutEditActivity.createIntent(context, position, shortcutId, appWidgetId)
        val data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), "$appWidgetId/$position")
        editIntent.data = data
        return PendingIntent.getActivity(context, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}