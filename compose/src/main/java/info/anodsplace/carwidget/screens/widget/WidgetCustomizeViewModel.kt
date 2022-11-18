package info.anodsplace.carwidget.screens.widget

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.screens.WidgetDialogEvent
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.viewmodel.BaseFlowViewModel
import okhttp3.internal.toHexString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

data class WidgetCustomizeState(
    val items: List<PreferenceItem> = listOf(),
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
)

sealed interface WidgetCustomizeEvent {
    class UpdateShortcutsNumber(val size: Int) : WidgetCustomizeEvent
    class ApplyChange(val key: String, val value: Any?) : WidgetCustomizeEvent
    class DialogEvent(val dialogEvent: WidgetDialogEvent) : WidgetCustomizeEvent
}

sealed interface WidgetCustomizeAction

fun createItems(settings: WidgetInterface) = listOf(
    PreferenceItem.Pick(
        titleRes = R.string.number,
        key = "cmp-number",
        entries = arrayOf("4","6","8","10"),
        entryValues = arrayOf("4","6","8","10"),
        value = settings.shortcutsNumber.toString()
    ),
    PreferenceItem.Color(
        titleRes = R.string.pref_bg_color_title,
        summary = "${settings.backgroundColor} - #${settings.backgroundColor.toHexString()}",
        key = "bg-color",
        color = Color(settings.backgroundColor)
    ),
    PreferenceItem.Category(
        titleRes = R.string.icon_style,
    ),
    PreferenceItem.Text(
        titleRes = R.string.icons_theme,
        key = "icons-theme"
    ),
    PreferenceItem.Switch(
        checked = settings.isIconsMono,
        titleRes = R.string.pref_icons_mono_title,
        key = "icons-mono"
    ),
    PreferenceItem.Color(
        titleRes = R.string.pref_tint_color_title,
        summaryRes = R.string.pref_tint_color_summary,
        key = "icons-color",
        color = settings.iconsColor?.let { Color(it) }
    ),
    PreferenceItem.Text(
        titleRes = R.string.pref_scale_icon,
        key = "icons-scale"
    ),
    PreferenceItem.Placeholder(
        titleRes = R.string.pref_font_size_title,
        summaryRes = R.string.pref_font_size_summary,
        key = "font-size"
    ),
    PreferenceItem.Color(
        titleRes = R.string.pref_font_color_title,
        summaryRes = R.string.pref_font_color_summary,
        key = "font-color",
        color = settings.fontColor?.let { Color(it) }
    ),
    PreferenceItem.Pick(
        titleRes = R.string.pref_rotate_icon_title,
        key = "icons-rotate",
        entriesRes = R.array.icon_rotate_titles,
        entryValuesRes = R.array.icon_rotate_values,
        value = settings.iconsRotate.name
    ),
    PreferenceItem.Placeholder(
        titleRes = R.string.adaptive_icon_style,
        key = "adaptive-icon-style",
    ),
    PreferenceItem.Switch(
        checked = settings.isTitlesHide,
        titleRes = R.string.pref_titles_hide_title,
        summaryRes = R.string.pref_titles_hide_summary,
        key = "titles-hide"
    ),
    PreferenceItem.Category(
        titleRes = R.string.transparent,
    ),
    PreferenceItem.CheckBox(
        checked = settings.isSettingsTransparent,
        titleRes = R.string.pref_settings_transparent,
        summaryRes = R.string.pref_settings_transparent_summary,
        key = "transparent-btn-settings"
    ),
    PreferenceItem.CheckBox(
        checked = settings.isIncarTransparent,
        titleRes = R.string.pref_incar_transparent,
        summaryRes = R.string.pref_incar_transparent_summary,
        key = "transparent-btn-incar"
    ),
)

class WidgetCustomizeViewModel(appWidgetIdScope: AppWidgetIdScope) : BaseFlowViewModel<WidgetCustomizeState, WidgetCustomizeEvent, WidgetCustomizeAction>(), KoinScopeComponent {

    class Factory(private val appWidgetIdScope: AppWidgetIdScope) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return WidgetCustomizeViewModel(appWidgetIdScope) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val widgetSettings: WidgetSettings by inject()

    init {
        viewState = WidgetCustomizeState(
            items = createItems(widgetSettings),
            widgetSettings = WidgetInterface.NoOp(widgetSettings)
        )
    }

    override fun handleEvent(event: WidgetCustomizeEvent) {
        when (event) {
            is WidgetCustomizeEvent.ApplyChange -> widgetSettings.applyChange(event.key, event.value)
            is WidgetCustomizeEvent.DialogEvent -> {
                when (val dialogEvent = event.dialogEvent) {
                    is WidgetDialogEvent.UpdateBackgroundColor -> {
                        if (dialogEvent.newColor != null) {
                            widgetSettings.backgroundColor = dialogEvent.newColor.value.toInt()
                        }
                    }
                    is WidgetDialogEvent.UpdateIconScale -> {
                        widgetSettings.iconsScale = dialogEvent.iconScale
                    }
                    is WidgetDialogEvent.UpdateTileColor -> {
                        if (dialogEvent.newColor != null) {
                            widgetSettings.tileColor = dialogEvent.newColor.value.toInt()
                        }
                    }
                }
            }
            is WidgetCustomizeEvent.UpdateShortcutsNumber -> {
                widgetSettings.shortcutsNumber = event.size
            }
        }
    }

}