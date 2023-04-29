package info.anodsplace.carwidget.utils

import android.content.Context
import android.view.InflateException
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun RemoteViews.render(context: Context): View = withContext(Dispatchers.Default) {
    try {
        return@withContext apply(context, null)
    } catch (e: InflateException) {
        AppLog.e("Cannot generate preview", e)
        return@withContext TextView(context).apply {
            text = context.getString(info.anodsplace.carwidget.content.R.string.cannot_generate_preview)
        }
    }
}