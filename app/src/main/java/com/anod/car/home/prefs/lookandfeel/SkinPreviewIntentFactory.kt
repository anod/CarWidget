package com.anod.car.home.prefs.lookandfeel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.anod.car.home.OverlayActivity
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus

class SkinPreviewIntentFactory(
    private val appWidgetIdScope: AppWidgetIdScope,
    private val context: Context
): PreviewPendingIntentFactory {

    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent = createEditWidgetButtonIntent(buttonId)
    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent = createEditWidgetButtonIntent(buttonId)
    override fun createNew(appWidgetId: Int, position: Int) = createEditIntent(appWidgetId, position, 0)
    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int, shortcutId: Long) = createEditIntent(appWidgetId, position, shortcutId)

    private fun createEditIntent(appWidgetId: Int, position: Int, shortcutId: Long)
        = createIntent(Deeplink.EditShortcut(appWidgetId, shortcutId, position))
    private fun createEditWidgetButtonIntent(buttonId: Int)
        = createIntent(Deeplink.EditWidgetButton(+appWidgetIdScope, buttonId))

    private fun createIntent(deeplink: Deeplink): PendingIntent {
        val buttonChoiceIntent = Intent(
            Intent.ACTION_VIEW,
            deeplink.toUri(),
            context,
            OverlayActivity::class.java
        ).also {
            it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, +appWidgetIdScope)
        }

        return PendingIntent.getActivity(context, 0, buttonChoiceIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}