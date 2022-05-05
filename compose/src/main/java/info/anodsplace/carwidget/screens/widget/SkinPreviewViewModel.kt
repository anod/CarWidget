package info.anodsplace.carwidget.screens.widget

import android.app.Application
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
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.ktx.hashCodeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

class SkinPreviewViewModel(application: Application, private val appWidgetId: Int): AndroidViewModel(application), KoinComponent {

    class Factory(private val appContext: Context, private val appWidgetId: Int): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SkinPreviewViewModel(appContext as Application, appWidgetId) as T
    }

    private val bitmapMemoryCache: BitmapLruCache by inject()
    private val db: ShortcutsDatabase by inject()

    val widgetSettings: WidgetInterface by inject(parameters = { parametersOf(appWidgetId) })
    val skinList = SkinList(widgetSettings.skin, application)
    val shortcuts = WidgetShortcutsModel(application, db, DefaultsResourceProvider(application), appWidgetId)
    val currentSkin = MutableStateFlow(skinList.current)
    val reload: Flow<Int> = widgetSettings.changes.onStart { emit(Pair("", null)) }
        .combine(db.observeTarget(appWidgetId).onStart { emit(emptyMap()) }) { s, t ->
            if (s.first.isEmpty() && t.isEmpty()) {
                0
            } else {
                hashCodeOf(s, t)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = 0)
        .filter { it != 0 }
        .onEach {
            delay(300)
        }

    override fun onCleared() {
        super.onCleared()
        bitmapMemoryCache.evictAll()
    }

    suspend fun load(overrideSkin: SkinList.Item, context: Context): View {
        // overrideSkin
        val intentFactory: PendingIntentFactory = get<PreviewPendingIntentFactory>(parameters = { parametersOf(appWidgetId) })
        val widgetView: WidgetView = get(parameters = { parametersOf(appWidgetId, bitmapMemoryCache, intentFactory, true) })
        return renderPreview(widgetView, overrideSkin.value, context)
    }

    private suspend fun renderPreview(widgetView: WidgetView, overrideSkin: String, context: Context): View = withContext(Dispatchers.Default) {
        val rv = widgetView.apply {
            this.overrideSkin = overrideSkin
            init()
        }.create()

        try {
            return@withContext rv.apply(context, null)
        } catch (e: InflateException) {
            AppLog.e("Cannot generate preview for $overrideSkin", e)
            return@withContext TextView(getApplication()).apply {
                text = context.getString(R.string.cannot_generate_preview)
            }
        }
    }
}