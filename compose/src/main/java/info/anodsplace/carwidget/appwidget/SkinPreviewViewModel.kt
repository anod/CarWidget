package info.anodsplace.carwidget.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.DummyWidgetShortcutsModel
import info.anodsplace.carwidget.skin.SkinPropertiesFactory
import info.anodsplace.carwidget.utils.render
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

@Immutable
data class SkinPreviewViewState(
    val skinList: SkinList = SkinList(WidgetInterface.skins, WidgetInterface.skins, 0),
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
    val previewVersion: Int = 0
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
        val shortcuts = DummyWidgetShortcutsModel(context, size = 4)
        shortcuts.createDefaultShortcuts()
        val widgetView: WidgetView = SkinWidgetView(
            skinProperties = skinProperties,
            shortcuts = shortcuts.shortcuts,
            context = context,
            widgetSettings = widgetSettings,
            appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID,
            pendingIntentFactory = intentFactory,
            inCarMode = false,
            inCarSettings = InCarInterface.NoOp(),
            iconLoader = ShortcutIconLoader.Activity(context)
        )
        return widgetView.create().render(context)
    }

    override fun handleEvent(event: SkinPreviewViewEvent) {

    }
}

class RealSkinPreviewViewModel(
    appWidgetIdScope: AppWidgetIdScope
): BaseFlowViewModel<SkinPreviewViewState, SkinPreviewViewEvent, SkinPreviewViewAction>(),
    SkinPreviewViewModel, KoinScopeComponent {

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
                    previewVersion = viewState.previewVersion + 1
                )
            }
        }

        viewModelScope.launch {
            db.observeTarget(+appWidgetIdScope).collect {
                viewState = viewState.copy(previewVersion = viewState.previewVersion + 1)
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
        val widgetView: WidgetView = get(parameters = { parametersOf(bitmapMemoryCache, intentFactory, true, overrideSkin.value, null) })
        return widgetView.create().render(context)
    }
}