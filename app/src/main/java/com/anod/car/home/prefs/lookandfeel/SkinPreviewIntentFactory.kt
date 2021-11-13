package com.anod.car.home.prefs.lookandfeel

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.anod.car.home.BuildConfig
import com.anod.car.home.R
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.screens.shortcuts.ShortcutEditFragment
import info.anodsplace.carwidget.screens.shortcuts.ShortcutPickerFragment
import info.anodsplace.carwidget.screens.widget.SkinList
import info.anodsplace.framework.app.FragmentContainerActivity

class SkinPreviewIntentFactory(
    private val appWidgetId: Int,
    private val skinItem: SkinList.Item,
    private val context: Context
): PreviewPendingIntentFactory {

    override fun createNew(appWidgetId: Int, position: Int): PendingIntent {
        val newIntent = FragmentContainerActivity.intent(
            context = context,
            factory = ShortcutPickerFragment.Factory(position, appWidgetId, R.style.Dialog)
        )
        newIntent.data = Uri.parse(
            "carwidget://${BuildConfig.APPLICATION_ID}/widget/$appWidgetId/shortcut/new/position/$position"
        )
        return PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity.createIntent(appWidgetId, skinItem.value, buttonId, context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity.createIntent(appWidgetId, skinItem.value, buttonId, context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int, shortcutId: Long): PendingIntent {
        val editIntent = FragmentContainerActivity.intent(
            context = context,
            factory = ShortcutEditFragment.Factory(position, shortcutId, appWidgetId)
        )
        editIntent.data = Uri.parse(
            "carwidget://${BuildConfig.APPLICATION_ID}/widget/$appWidgetId/shortcut/$shortcutId/position/$position"
        )
        return PendingIntent.getActivity(context, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}