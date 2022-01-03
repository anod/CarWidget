package com.anod.car.home.appwidget

import com.anod.car.home.ShortcutActivity
import com.anod.car.home.incar.ModeService

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.anod.car.home.OverlayActivity
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.utils.forSettings

class ShortcutPendingIntent(private val context: Context, private val shortcutResources: ShortcutResources) : PendingIntentFactory {

    private val inCarOnIntent: PendingIntent
        get() {
            val onIntent = Intent(context, ModeService::class.java)
            onIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON)
            onIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true)
            onIntent.data = Deeplink.SwitchMode(true).toUri()
            return PendingIntent.getService(context, 0, onIntent, PendingIntent.FLAG_IMMUTABLE)
        }

    private val inCarOffIntent: PendingIntent
        get() {
            val offIntent = Intent(context, ModeService::class.java)
            offIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF)
            offIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true)
            offIntent.data = Deeplink.SwitchMode(false).toUri()
            return PendingIntent.getService(context, 0, offIntent, PendingIntent.FLAG_IMMUTABLE)
        }

    override fun createNew(appWidgetId: Int, position: Int): PendingIntent {
        val editIntent = Intent(
                Intent.ACTION_VIEW,
                Deeplink.EditShortcut(appWidgetId, 0, position).toUri(),
                context,
                OverlayActivity::class.java
        ).also {
            it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        return PendingIntent.getActivity(context, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    /**
     * Create an Intent to launch Configuration
     */
    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent {
        val intent = Intent().forSettings(context, appWidgetId, shortcutResources)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int, shortcutId: Long): PendingIntent? {
        return createShortcut(intent) { Deeplink.OpenWidgetShortcut(appWidgetId, position).toUri() }
    }

    fun createShortcut(intent: Intent, uri: () -> Uri): PendingIntent? {
        val action = intent.action ?: ""
        val isCall = INTENT_ACTION_CALL_PRIVILEGED == action || Intent.ACTION_CALL == action
        if (intent.extras == null && !isCall) { // Samsung s3 bug
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val data = uri()

        if (action == ShortcutExtra.ACTION_MEDIA_BUTTON) {
            intent.data = data
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val launchIntent = Intent(context, ShortcutActivity::class.java)
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        launchIntent.data = data
        launchIntent.action = Intent.ACTION_MAIN
        launchIntent.putExtra(ShortcutActivity.EXTRA_INTENT, intent)
        return PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent {
        return if (on) {
            inCarOnIntent
        } else inCarOffIntent
    }

    companion object {
        const val INTENT_ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED"
    }
}
