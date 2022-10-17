package info.anodsplace.carwidget.screens.widget

import android.app.UiModeManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.compose.*

@Composable
fun FontSize(
    paddingValues: PaddingValues,
    initialValue: Int,
    onValueChanged: (Int) -> Unit,
    placeholder: PreferenceItem.Placeholder
) {
    PreferenceSlider(
        paddingValues = paddingValues,
        initialValue = initialValue,
        onValueChanged = onValueChanged,
        item = placeholder.toTextItem(),
        suffixText = { Text(text = "px")}
    )
}

private fun createItems(settings: WidgetInterface) = listOf(
    PreferenceItem.Text(
        titleRes = R.string.pref_tint_color_title,
        summaryRes = R.string.pref_tint_color_summary,
        key = "icons-color"
    ),
    PreferenceItem.Placeholder(
        titleRes = R.string.pref_font_size_title,
        summaryRes = R.string.pref_font_size_summary,
        key = "font-size"
    ),
    PreferenceItem.Text(
        titleRes = R.string.pref_font_color_title,
        summaryRes = R.string.pref_font_color_summary,
        key = "font-color"
    ),
    PreferenceItem.Pick(
        titleRes = R.string.pref_rotate_icon_title,
        key = "icons-rotate",
        entriesRes = R.array.icon_rotate_titles,
        entryValuesRes = R.array.icon_rotate_values
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

@Composable
fun WidgetLookMoreScreen(screenState: WidgetLookMoreState, onEvent: (WidgetLookMoreEvent) -> Unit, innerPadding: PaddingValues = PaddingValues(0.dp)) {
    Surface {
        PreferencesScreen(
            modifier = Modifier.padding(innerPadding),
            preferences = createItems(screenState.widgetSettings),
            onClick = { item ->
                when (item) {
                    is PreferenceItem.CheckBox -> {
                        onEvent(WidgetLookMoreEvent.ApplyChange(item.key, item.checked))
                    }
                    is PreferenceItem.Switch -> {
                        onEvent(WidgetLookMoreEvent.ApplyChange(item.key, item.checked))
                    }
                    else -> {}
                }
            }
        ) { item, paddingValues ->
            when (item.key) {
                "font-size" -> {
                    FontSize(
                        paddingValues = paddingValues,
                        initialValue = screenState.widgetSettings.fontSize,
                        onValueChanged = { onEvent(WidgetLookMoreEvent.ApplyChange("font-size", it)) },
                        placeholder = item as PreferenceItem.Placeholder
                    )
                }
                "adaptive-icon-style" -> Preference(
                    item,
                    secondary = {
                        Column {
                            IconShapeSelector(
                                names =  stringArrayResource(id = R.array.adaptive_icon_style_names),
                                pathMasks = stringArrayResource(id = R.array.adaptive_icon_style_paths_values),
                                selected = screenState.widgetSettings.adaptiveIconStyle,
                                defaultSystemMask = "",
                                systemMaskName = "",
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                onPathChange = { newPath -> onEvent(WidgetLookMoreEvent.ApplyChange("adaptive-icon-style", newPath)) }
                            )
                        }
                    },
                    onClick = { })
            }
        }
    }
}

@Preview("WidgetLookMoreScreen Dark")
@Composable
fun WidgetLookMoreScreenDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        WidgetLookMoreScreen(
            screenState = WidgetLookMoreState(widgetSettings = WidgetInterface.NoOp()),
            onEvent = { }
        )
    }
}