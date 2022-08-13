package info.anodsplace.carwidget.appwidget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

interface PendingIntentFactory {
    fun createNew(appWidgetId: Int, position: Int): PendingIntent
    fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent
    fun createShortcut(
        intent: Intent,
        appWidgetId: Int,
        position: Int,
        shortcutId: Long
    ): PendingIntent?

    fun createInCar(on: Boolean, buttonId: Int): PendingIntent

    class NoOp(context: Context) : PendingIntentFactory {
        private val intent = PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)!!
        override fun createNew(appWidgetId: Int, position: Int) = intent
        override fun createSettings(appWidgetId: Int, buttonId: Int) = intent
        override fun createShortcut(
            intent: Intent,
            appWidgetId: Int,
            position: Int,
            shortcutId: Long
        ) = null
        override fun createInCar(on: Boolean, buttonId: Int): PendingIntent = intent
    }
}

interface PreviewPendingIntentFactory: PendingIntentFactory