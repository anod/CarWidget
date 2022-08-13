package info.anodsplace.carwidget.screens.widget

import android.app.Application
import android.content.Context
import android.view.InflateException
import android.view.View
import android.widget.RemoteViews
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
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.ktx.hashCodeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

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

interface SkinPreviewViewModel {
    class Factory(private val appContext: Context, private val appWidgetIdScope: AppWidgetIdScope?):
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T  {
            return if (appWidgetIdScope == null)
                DummySkinPreviewViewModel(appContext.applicationContext as Application) as T
            else
                RealSkinPreviewViewModel(appContext.applicationContext as Application, appWidgetIdScope) as T
        }
    }

    val skinList: SkinList
    val currentSkin: MutableStateFlow<SkinList.Item>
    val widgetSettings: WidgetInterface
    val reload: Flow<Int>

    suspend fun load(overrideSkin: SkinList.Item, context: Context): View
}

class DummySkinPreviewViewModel(application: Application): AndroidViewModel(application), SkinPreviewViewModel, KoinComponent {
    private val bitmapMemoryCache: BitmapLruCache by inject()
    override val widgetSettings: WidgetInterface = WidgetInterface.NoOp()
    override val skinList = SkinList(widgetSettings.skin, application)
    override val currentSkin = MutableStateFlow(skinList.current)
    override val reload: Flow<Int> = flowOf()

    override suspend fun load(overrideSkin: SkinList.Item, context: Context): View {
//        val intentFactory: PendingIntentFactory = PendingIntentFactory.NoOp(context)
//        val widgetView: WidgetView = get(parameters = { parametersOf(bitmapMemoryCache, intentFactory, true, overrideSkin.value) })
//        val remoteViews = widgetView.create()
//        return renderPreview(remoteViews, context)
        return View(context)
    }
}

class RealSkinPreviewViewModel(application: Application, appWidgetIdScope: AppWidgetIdScope): AndroidViewModel(application), SkinPreviewViewModel, KoinScopeComponent {

    override val scope: Scope = appWidgetIdScope.scope

    private val bitmapMemoryCache: BitmapLruCache by inject()
    private val db: ShortcutsDatabase by inject()
    override val widgetSettings: WidgetInterface by inject()

    override val skinList = SkinList(widgetSettings.skin, application)
    override val currentSkin = MutableStateFlow(skinList.current)

    override val reload: Flow<Int> = widgetSettings.changes.onStart { emit(Pair("", null)) }
        .combine(db.observeTarget(+appWidgetIdScope).onStart { emit(emptyMap()) }) { s, t ->
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

    override suspend fun load(overrideSkin: SkinList.Item, context: Context): View {
        val intentFactory: PendingIntentFactory = get<PreviewPendingIntentFactory>()
        val widgetView: WidgetView = get(parameters = { parametersOf(bitmapMemoryCache, intentFactory, true, overrideSkin.value) })
        val remoteViews = widgetView.create()
        return renderPreview(remoteViews, context)
    }
}

private suspend fun renderPreview(remoteViews: RemoteViews, context: Context): View = withContext(Dispatchers.Default) {
    try {
        return@withContext remoteViews.apply(context, null)
    } catch (e: InflateException) {
        AppLog.e("Cannot generate preview", e)
        return@withContext TextView(context).apply {
            text = context.getString(R.string.cannot_generate_preview)
        }
    }
}