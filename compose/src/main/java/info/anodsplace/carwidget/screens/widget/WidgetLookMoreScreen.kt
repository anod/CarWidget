package info.anodsplace.carwidget.screens.widget

import android.app.UiModeManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferenceSlider
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.compose.toTextItem

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
    PreferenceItem.List(
        titleRes = R.string.pref_rotate_icon_title,
        key = "icons-rotate",
        entries = R.array.icon_rotate_titles,
        entryValues = R.array.icon_rotate_values
    ),
    PreferenceItem.List(
        titleRes = R.string.adaptive_icon_style,
        key = "adaptive-icon-style",
        entries = R.array.adaptive_icon_style_names,
        entryValues = R.array.adaptive_icon_style_paths_values
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
fun WidgetLookMoreScreen(modifier: Modifier, settings: WidgetInterface) {
    PreferencesScreen(
            modifier = modifier,
            preferences = createItems(settings),
            categoryColor = MaterialTheme.colorScheme.secondary,
            descriptionColor = MaterialTheme.colorScheme.onBackground,
            onClick = { item ->
                when (item) {
                    is PreferenceItem.CheckBox -> {
                        settings.applyChange(item.key, item.checked)
                    }
                    is PreferenceItem.Switch -> {
                        settings.applyChange(item.key, item.checked)
                    }
                    else -> {}
                }
            }
    ) { placeholder, paddingValues ->
        when (placeholder.key) {
            "font-size" -> {
                FontSize(
                        paddingValues = paddingValues,
                        initialValue = settings.fontSize,
                        onValueChanged = { settings.fontSize = it },
                        placeholder = placeholder as PreferenceItem.Placeholder
                )
            }
        }
    }
}

@Preview("WidgetLookMoreScreen Dark")
@Composable
fun WidgetLookMoreScreenDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        WidgetLookMoreScreen(Modifier, WidgetInterface.NoOp())
    }
}