package info.anodsplace.carwidget.appwidget

import android.app.PendingIntent
import android.content.Intent

interface PendingIntentFactory {
    fun createNew(appWidgetId: Int, position: Int): PendingIntent
    fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent
    fun createShortcut(intent: Intent, appWidgetId: Int, position: Int, shortcutId: Long): PendingIntent?
    fun createInCar(on: Boolean, buttonId: Int): PendingIntent
}

interface PreviewPendingIntentFactory: PendingIntentFactory