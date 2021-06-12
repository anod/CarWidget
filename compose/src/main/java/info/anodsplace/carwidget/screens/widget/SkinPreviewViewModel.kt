package info.anodsplace.carwidget.screens.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.InflateException
import android.view.View
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class SkinList(skin: String, context: Context) {
    class Item(val title: String, val value: String)

    private val values = listOf("you", "cards", "holo", "glossy", "carhome", "windows7", "blackbearblanc")
    internal val titles = context.resources.getStringArray(R.array.skin_titles)
    val selectedSkinPosition: Int = values.indexOf(skin)
    val count: Int = values.size

    operator fun get(position: Int): Item = Item(titles[position], values[position])
}

class SkinPreviewViewModel(application: Application): AndroidViewModel(application), KoinComponent {
    var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private val bitmapMemoryCache: BitmapLruCache by inject()
    val skinList: SkinList by lazy { SkinList(widgetSettings.skin, application) }
    val shortcuts: WidgetShortcutsModel by lazy { WidgetShortcutsModel(application, DefaultsResourceProvider(application), appWidgetId) }
    val widgetSettings: WidgetInterface by inject(parameters = { parametersOf(appWidgetId) })

    override fun onCleared() {
        super.onCleared()
        bitmapMemoryCache.evictAll()
    }

    fun load(overrideSkin: SkinList.Item): Flow<View> = flow {
        val intentFactory: PendingIntentFactory = get<PreviewPendingIntentFactory>(parameters = { parametersOf(appWidgetId, overrideSkin) })
        val widgetView: WidgetView = get(parameters = { parametersOf(appWidgetId, bitmapMemoryCache, intentFactory, true) })
        val preview = renderPreview(widgetView, overrideSkin.value)
        emit(preview)
    }

    private suspend fun renderPreview(widgetView: WidgetView, overrideSkin: String): View = withContext(Dispatchers.Default) {
        val rv = widgetView.apply {
            init(overrideSkin)
        }.create()
        try {
            return@withContext rv.apply(getApplication(), null)
        } catch (e: InflateException) {
            AppLog.e("Cannot generate preview for $overrideSkin", e)
            return@withContext TextView(getApplication()).apply {
                text = context.getString(R.string.cannot_generate_preview)
            }
        }
    }
}