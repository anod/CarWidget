package info.anodsplace.carwidget.appwidget

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.utils.render
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.isNotVisible
import info.anodsplace.compose.isVisible
import info.anodsplace.compose.toColorHex
import info.anodsplace.framework.content.ScreenCommonAction
import info.anodsplace.framework.content.forStoreSearch
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

@Immutable
data class WidgetCustomizeState(
    val items: List<PreferenceItem> = listOf(),
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
    val skinList: SkinList = SkinList(WidgetInterface.skins, WidgetInterface.skins, 0),
    val previewVersion: Int = 0,
    val showBackgroundColorPicker: Boolean = false,
    val showFontColorPicker: Boolean = false,
    val showTileColorPicker: Boolean = false,
    val showIconsThemePicker: Boolean = false,
    val showIconsColorPicker: Boolean = false,
)

sealed interface WidgetCustomizeEvent {
    data object DownloadIconsTheme : WidgetCustomizeEvent
    class UpdateShortcutsNumber(val size: Int) : WidgetCustomizeEvent
    class ApplyChange(val key: String, val value: Any?) : WidgetCustomizeEvent
    class ShowBackgroundColorPicker(val show: Boolean) : WidgetCustomizeEvent
    class ShowIconsThemePicker(val show: Boolean) : WidgetCustomizeEvent
    class ShowFontColorPicker(val show: Boolean) : WidgetCustomizeEvent
    class ShowTileColorPicker(val show: Boolean) : WidgetCustomizeEvent
    class ShowIconsColorPicker(val show: Boolean) : WidgetCustomizeEvent
}

fun createItems(settings: WidgetInterface, skinList: SkinList, iconsThemeAppName: String) = listOf(
    PreferenceItem.Pick(
        titleRes = info.anodsplace.carwidget.content.R.string.skin,
        key = "skin",
        entries = skinList.titles.toTypedArray(),
        entryValues = skinList.values.toTypedArray(),
        value = skinList.current.value
    ),
    PreferenceItem.Pick(
        titleRes = info.anodsplace.carwidget.content.R.string.number_shortcuts_title,
        key = "cmp-number",
        entries = arrayOf("4","6","8","10","12","14"),
        entryValues = arrayOf("4","6","8","10","12","14"),
        value = settings.shortcutsNumber.toString()
    ),
    Color(settings.backgroundColor).let { backgroundColor ->
        PreferenceItem.Color(
            titleRes = info.anodsplace.carwidget.content.R.string.pref_bg_color_title,
            summary = if (backgroundColor.isVisible) "#${backgroundColor.toColorHex()}" else "",
            summaryRes = if (backgroundColor.isNotVisible) info.anodsplace.carwidget.content.R.string.pref_bg_color_summary else 0,
            key = "bg-color",
            color = backgroundColor
        )
    },
    PreferenceItem.Switch(
        checked = settings.paletteBackground,
        titleRes = info.anodsplace.carwidget.content.R.string.auto_color,
        key = "palette-background",
        enabled = skinList.current.value == WidgetInterface.SKIN_WINDOWS7
    ),
    Color(settings.tileColor).let { tileColor ->
        PreferenceItem.Color(
            titleRes = info.anodsplace.carwidget.content.R.string.pref_tile_color_title,
            summary = if (tileColor.isVisible) "#${tileColor.toColorHex()}" else "",
            key = "button-color",
            color = tileColor,
            enabled = skinList.current.value == WidgetInterface.SKIN_WINDOWS7 && !settings.paletteBackground
        )
    },
    PreferenceItem.Spacer(),
    PreferenceItem.Category(
        titleRes = info.anodsplace.carwidget.content.R.string.icon_style,
    ),
    PreferenceItem.Text(
        titleRes = info.anodsplace.carwidget.content.R.string.icons_theme,
        key = "icons-theme",
        summaryRes = if (settings.iconsTheme.isEmpty()) info.anodsplace.carwidget.content.R.string.none else 0,
        summary =  if (settings.iconsTheme.isEmpty()) "" else iconsThemeAppName
    ),
    PreferenceItem.Switch(
        checked = settings.isIconsMono,
        titleRes = info.anodsplace.carwidget.content.R.string.pref_icons_mono_title,
        key = "icons-mono"
    ),
    PreferenceItem.Color(
        titleRes = info.anodsplace.carwidget.content.R.string.pref_tint_color_title,
        summaryRes = info.anodsplace.carwidget.content.R.string.pref_tint_color_summary,
        key = "icons-color",
        color = settings.iconsColor?.let { Color(it) }
    ),
    PreferenceItem.Pick(
        titleRes = info.anodsplace.carwidget.content.R.string.pref_scale_icon,
        key = "icons-scale",
        entryValues = arrayOf("0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"),
        entries = arrayOf("x1.0","x1.1","x1.2","x1.3","x1.4","x1.5","x1.6","x1.7","x1.8","x1.9","x2.0","x2.1","x2.2","x2.3","x2.4","x2.5","x2.6","x2.7","x2.8","x2.9","x3.0"),
        value = settings.iconsScale
    ),
    PreferenceItem.Pick(
        titleRes = info.anodsplace.carwidget.content.R.string.pref_rotate_icon_title,
        key = "icons-rotate",
        entriesRes = info.anodsplace.carwidget.content.R.array.icon_rotate_titles,
        entryValuesRes = info.anodsplace.carwidget.content.R.array.icon_rotate_values,
        value = settings.iconsRotate.name
    ),
    PreferenceItem.Placeholder(
        titleRes = info.anodsplace.carwidget.content.R.string.adaptive_icon_style,
        key = "adaptive-icon-style",
    ),
    PreferenceItem.Spacer(),
    PreferenceItem.Category(
        titleRes = info.anodsplace.carwidget.content.R.string.title,
    ),
    PreferenceItem.Switch(
        checked = settings.isTitlesHide,
        titleRes = info.anodsplace.carwidget.content.R.string.pref_titles_hide_title,
        summaryRes = info.anodsplace.carwidget.content.R.string.pref_titles_hide_summary,
        key = "titles-hide"
    ),
    PreferenceItem.Placeholder(
        titleRes = info.anodsplace.carwidget.content.R.string.pref_font_size_title,
        summaryRes = info.anodsplace.carwidget.content.R.string.pref_font_size_summary,
        key = "font-size"
    ),
    PreferenceItem.Color(
        titleRes = info.anodsplace.carwidget.content.R.string.pref_font_color_title,
        summaryRes = info.anodsplace.carwidget.content.R.string.pref_font_color_summary,
        key = "font-color",
        color = settings.fontColor?.let { Color(it) }
    ),
)

class WidgetCustomizeViewModel(appWidgetIdScope: AppWidgetIdScope) : BaseFlowViewModel<WidgetCustomizeState, WidgetCustomizeEvent, ScreenCommonAction>(), KoinScopeComponent,
    SkinViewFactory {

    class Factory(private val appWidgetIdScope: AppWidgetIdScope) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return WidgetCustomizeViewModel(appWidgetIdScope) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val widgetSettings: WidgetSettings by inject()
    private val context: Context by inject()
    private val bitmapMemoryCache: BitmapLruCache by inject()
    private val packageManager = context.packageManager

    init {
        val skinList = SkinList(widgetSettings.skin, context)
        viewState = WidgetCustomizeState(
            items = createItems(widgetSettings, skinList, if (widgetSettings.iconsTheme.isEmpty()) "" else packageManager.getAppTitle(widgetSettings.iconsTheme)),
            widgetSettings = WidgetInterface.NoOp(widgetSettings),
            skinList = skinList,
        )

        viewModelScope.launch {
            widgetSettings.changes.collect {
                val newSkinList = if (viewState.skinList.current.value != widgetSettings.skin)
                    viewState.skinList.copy(selectedSkinPosition = WidgetInterface.skins.indexOf(widgetSettings.skin))
                else
                    viewState.skinList
                viewState = viewState.copy(
                    skinList = newSkinList,
                    widgetSettings = WidgetInterface.NoOp(widgetSettings),
                    items = createItems(widgetSettings, newSkinList, if (widgetSettings.iconsTheme.isEmpty()) "" else packageManager.getAppTitle(widgetSettings.iconsTheme)),
                    previewVersion = viewState.previewVersion + 1
                )
            }
        }
    }

    override fun handleEvent(event: WidgetCustomizeEvent) {
        when (event) {
            is WidgetCustomizeEvent.ApplyChange -> widgetSettings.applyChange(event.key, event.value)
            is WidgetCustomizeEvent.UpdateShortcutsNumber -> {
                widgetSettings.shortcutsNumber = event.size
            }
            is WidgetCustomizeEvent.ShowBackgroundColorPicker -> {
                viewState = viewState.copy(showBackgroundColorPicker = event.show)
            }
            is WidgetCustomizeEvent.ShowFontColorPicker -> {
                viewState = viewState.copy(showFontColorPicker = event.show)
            }
            is WidgetCustomizeEvent.ShowIconsThemePicker -> {
                viewState = viewState.copy(showIconsThemePicker = event.show)
            }
            is WidgetCustomizeEvent.ShowTileColorPicker -> {
                viewState = viewState.copy(showTileColorPicker = event.show)
            }
            is WidgetCustomizeEvent.ShowIconsColorPicker -> {
                viewState = viewState.copy(showIconsColorPicker = event.show)
            }

            WidgetCustomizeEvent.DownloadIconsTheme -> {
                emitAction(ScreenCommonAction.StartActivity(Intent().forStoreSearch(query = "Icons Pack")))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (scope.isNotClosed()) {
            bitmapMemoryCache.evictAll()
        }
    }

    override suspend fun create(overrideSkin: SkinList.Item): View {
        val intentFactory: PendingIntentFactory = PendingIntentFactory.NoOp(context)
        val widgetView: WidgetView = get(parameters = { parametersOf(bitmapMemoryCache, intentFactory, true, overrideSkin.value, 2) })
        return widgetView.create().render(context)
    }
}