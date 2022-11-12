package info.anodsplace.carwidget.screens.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.InflateException
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.appwidget.SkinWidgetView
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.DummyWidgetShortcutsModel
import info.anodsplace.carwidget.skin.SkinPropertiesFactory
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

data class SkinPreviewViewState(
    val skinList: SkinList,
    val currentSkin: SkinList.Item,
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
    val widgetShortcuts: Map<Int, Shortcut?>? = null,
    val reload: Int = 0
)

sealed interface SkinPreviewViewEvent {
    class UpdateCurrentSkin(val index: Int) : SkinPreviewViewEvent
    object Reload : SkinPreviewViewEvent
}

sealed interface SkinPreviewViewAction

interface SkinViewFactory {
    suspend fun create(overrideSkin: SkinList.Item): View
}

interface SkinPreviewViewModel : SkinViewFactory {
    class Factory(private val appWidgetIdScope: AppWidgetIdScope): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) = RealSkinPreviewViewModel(appWidgetIdScope) as T
    }

    val viewActions: Flow<SkinPreviewViewAction>
    val viewStates: Flow<SkinPreviewViewState>
    val viewState: SkinPreviewViewState

    fun handleEvent(event: SkinPreviewViewEvent)
}

class DummySkinPreviewViewModel(private val context: Context): ViewModel(), SkinPreviewViewModel {
    private val widgetSettings: WidgetInterface = WidgetInterface.NoOp()
    private val skinProperties: SkinProperties = SkinPropertiesFactory(context).create(widgetSettings.skin)

    override val viewActions: Flow<SkinPreviewViewAction> = emptyFlow()
    override val viewState : SkinPreviewViewState
    override val viewStates: Flow<SkinPreviewViewState>

    init {
        val skinList = SkinList(widgetSettings.skin, context)
        viewState = SkinPreviewViewState(
            skinList = skinList,
            currentSkin = skinList.current
        )
        viewStates = flowOf(viewState)
    }

    override suspend fun create(overrideSkin: SkinList.Item): View {
        val intentFactory: PendingIntentFactory = PendingIntentFactory.NoOp(context)
        val shortcuts = DummyWidgetShortcutsModel(context)
        shortcuts.init()
        val widgetView: WidgetView = SkinWidgetView(
            skinProperties = skinProperties,
            shortcuts = shortcuts,
            context = context,
            widgetSettings = widgetSettings,
            appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID,
            pendingIntentFactory = intentFactory,
            inCarMode = false,
            inCarSettings = InCarInterface.NoOp(),
            iconLoader = ShortcutIconLoader.Activity(context)
        )
        val remoteViews = widgetView.create()
        return renderPreview(remoteViews, context)
    }

    override fun handleEvent(event: SkinPreviewViewEvent) {

    }
}

class RealSkinPreviewViewModel(appWidgetIdScope: AppWidgetIdScope): BaseFlowViewModel<SkinPreviewViewState, SkinPreviewViewEvent, SkinPreviewViewAction>(), SkinPreviewViewModel, KoinScopeComponent {

    override val scope: Scope = appWidgetIdScope.scope

    private val context: Context by inject()
    private val bitmapMemoryCache: BitmapLruCache by inject()
    private val db: ShortcutsDatabase by inject()
    private val widgetSettings: WidgetSettings by inject()

    init {
        val skinList = SkinList(widgetSettings.skin, context)
        viewState = SkinPreviewViewState(
            skinList = skinList,
            currentSkin = skinList.current,
            widgetSettings = WidgetInterface.NoOp(widgetSettings)
        )

        viewModelScope.launch {
            widgetSettings.changes.collect {
                viewState = viewState.copy(
                    widgetSettings = WidgetInterface.NoOp(widgetSettings),
                    reload = viewState.reload + 1
                )
            }
        }

        viewModelScope.launch {
            db.observeTarget(+appWidgetIdScope).collect {
                viewState = viewState.copy(
                    widgetShortcuts = it,
                    reload = viewState.reload + 1
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bitmapMemoryCache.evictAll()
    }

    override fun handleEvent(event: SkinPreviewViewEvent) {
        when (event) {
            SkinPreviewViewEvent.Reload -> {

            }
            is SkinPreviewViewEvent.UpdateCurrentSkin -> {
                viewState = viewState.copy(
                    currentSkin = viewState.skinList[event.index]
                )
            }
        }
    }

    override suspend fun create(overrideSkin: SkinList.Item): View {
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