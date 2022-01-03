package com.anod.car.home.prefs.lookandfeel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.anod.car.home.OverlayActivity
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.screens.widget.SkinList

class SkinPreviewIntentFactory(
    private val appWidgetId: Int,
    private val skinItem: SkinList.Item,
    private val context: Context
): PreviewPendingIntentFactory {

    override fun createNew(appWidgetId: Int, position: Int) = createEditIntent(appWidgetId, position, 0)

    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity.createIntent(appWidgetId, skinItem.value, buttonId, context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity.createIntent(appWidgetId, skinItem.value, buttonId, context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int, shortcutId: Long) = createEditIntent(appWidgetId, position, shortcutId)

    private fun createEditIntent(appWidgetId: Int, position: Int, shortcutId: Long): PendingIntent {
        val editIntent = Intent(
            Intent.ACTION_VIEW,
            Deeplink.EditShortcut(appWidgetId, shortcutId, position).toUri(),
            context,
            OverlayActivity::class.java
        ).also {
            it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        return PendingIntent.getActivity(context, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}