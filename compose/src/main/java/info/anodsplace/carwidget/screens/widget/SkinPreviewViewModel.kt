package info.anodsplace.carwidget.screens.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.InflateException
import android.view.View
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.carwidget.screens.WidgetActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

data class SkinList(
        val values: List<String>,
        val titles: List<String>,
        val selectedSkinPosition: Int
) {
    data class Item(val title: String, val value: String)

    val count = values.size
    val current: Item = Item(titles[selectedSkinPosition], values[selectedSkinPosition])

    constructor(skin: String, context: Context) : this(
            values = WidgetInterface.skins,
            titles = context.resources.getStringArray(R.array.skin_titles).toList(),
            selectedSkinPosition = WidgetInterface.skins.indexOf(skin)
    )

    operator fun get(position: Int): Item = Item(titles[position], values[position])
}

class SkinPreviewViewModel(application: Application, var appWidgetId: Int): AndroidViewModel(application), KoinComponent {
    private val bitmapMemoryCache: BitmapLruCache by inject()

    val widgetSettings: WidgetInterface by inject(parameters = { parametersOf(appWidgetId) })
    val skinList = SkinList(widgetSettings.skin, application)
    val shortcuts: WidgetShortcutsModel by lazy { WidgetShortcutsModel(application, DefaultsResourceProvider(application), appWidgetId) }
    val currentSkin = MutableStateFlow(skinList.current)

    class Factory(private val appContext: Context, private val appWidgetId: Int): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = SkinPreviewViewModel(appContext as Application, appWidgetId) as T
    }

    override fun onCleared() {
        super.onCleared()
        bitmapMemoryCache.evictAll()
    }

    suspend fun load(overrideSkin: SkinList.Item): View? {
        val intentFactory: PendingIntentFactory = get<PreviewPendingIntentFactory>(parameters = { parametersOf(appWidgetId, overrideSkin) })
        val widgetView: WidgetView = get(parameters = { parametersOf(appWidgetId, bitmapMemoryCache, intentFactory, true) })
        return renderPreview(widgetView, overrideSkin.value)
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