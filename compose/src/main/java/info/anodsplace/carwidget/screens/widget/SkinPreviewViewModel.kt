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

data class SkinPreviewViewState(
    val skinList: SkinList = SkinList(WidgetInterface.skins, WidgetInterface.skins, 0),
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
    val widgetShortcuts: Map<Int, Shortcut?> = mapOf(),
)

sealed interface SkinPreviewViewEvent

sealed interface SkinPreviewViewAction

interface SkinViewFactory {
    suspend fun create(overrideSkin: SkinList.Item): View
}

interface SkinPreviewViewModel : SkinViewFactory {
    class Factory(private val appWidgetIdScope: AppWidgetIdScope): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
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
    override val viewState : SkinPreviewViewState = SkinPreviewViewState()
    override val viewStates: Flow<SkinPreviewViewState> = flowOf(viewState)

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

class RealSkinPreviewViewModel(
    appWidgetIdScope: AppWidgetIdScope
): BaseFlowViewModel<SkinPreviewViewState, SkinPreviewViewEvent, SkinPreviewViewAction>(), SkinPreviewViewModel, KoinScopeComponent {

    override val scope: Scope = appWidgetIdScope.scope

    private val context: Context by inject()
    private val bitmapMemoryCache: BitmapLruCache by inject()
    private val db: ShortcutsDatabase by inject()
    private val widgetSettings: WidgetSettings by inject()

    init {
        viewState = SkinPreviewViewState(
            skinList = SkinList(widgetSettings.skin, context),
            widgetSettings = WidgetInterface.NoOp(widgetSettings)
        )

        viewModelScope.launch {
            widgetSettings.changes.collect {
                val skinList = if (viewState.skinList.current.value != widgetSettings.skin)
                    viewState.skinList.copy(selectedSkinPosition = WidgetInterface.skins.indexOf(widgetSettings.skin))
                else
                    viewState.skinList
                viewState = viewState.copy(
                    skinList = skinList,
                    widgetSettings = WidgetInterface.NoOp(widgetSettings),
                )
            }
        }

        viewModelScope.launch {
            db.observeTarget(+appWidgetIdScope).collect {
                viewState = viewState.copy(
                    widgetShortcuts = it,
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bitmapMemoryCache.evictAll()
    }

    override fun handleEvent(event: SkinPreviewViewEvent) {

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