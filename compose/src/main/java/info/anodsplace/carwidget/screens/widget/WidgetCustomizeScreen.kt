package info.anodsplace.carwidget.screens.widget

import android.app.UiModeManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.TextDecreaseIcon
import info.anodsplace.carwidget.TextIncreaseIcon
import info.anodsplace.carwidget.content.R
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
        suffixText = { Text(text = "px") },
        startIcon = { TextDecreaseIcon() },
        endIcon = { TextIncreaseIcon() },
    )
}

@Composable
fun WidgetCustomizeScreen(
    screenState: WidgetCustomizeState,
    onEvent: (WidgetCustomizeEvent) -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    isCompact: Boolean = false,
    skinViewFactory: SkinViewFactory
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        if (!isCompact) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(208.dp)
                    .padding(4.dp)
            ) {
                WidgetSkinPreview(
                    skinItem = screenState.skinList.current,
                    skinViewFactory = skinViewFactory,
                    reload = screenState.previewVersion
                )
            }
        }
        Surface {
            PreferencesScreen(
                preferences = screenState.items,
                onClick = { item ->
                    when (item.key) {
                        "bg-color" -> onEvent(WidgetCustomizeEvent.ShowBackgroundColorPicker(show = true))
                        "icons-theme" -> onEvent(WidgetCustomizeEvent.ShowIconsThemePicker(show = true))
                        "font-color" -> onEvent(WidgetCustomizeEvent.ShowFontColorPicker(show = true))
                        "button-color" -> onEvent(WidgetCustomizeEvent.ShowTileColorPicker(show = true))
                        "icons-color" -> onEvent(WidgetCustomizeEvent.ShowIconsColorPicker(show = true))
                        "cmp-number" -> onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.value.toInt()))
                        "titles-hide" -> {
                            if (screenState.widgetSettings.fontSize == 0 && !item.checked) {
                                onEvent(WidgetCustomizeEvent.ApplyChange("font-size", WidgetInterface.FONT_SIZE_UNDEFINED))
                            }
                            onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.checked))
                        }
                        else -> {
                            when (item) {
                                is PreferenceItem.CheckBox -> {
                                    onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.checked))
                                }

                                is PreferenceItem.Switch -> {
                                    onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.checked))
                                }

                                is PreferenceItem.Pick -> {
                                    onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.value))
                                }

                                else -> { }
                            }
                        }
                    }
                }
            ) { item, paddingValues ->
                when (item.key) {
                    "font-size" -> {
                        FontSize(
                            paddingValues = paddingValues,
                            initialValue = screenState.widgetSettings.fontSize,
                            onValueChanged = { onEvent(WidgetCustomizeEvent.ApplyChange("font-size", it)) },
                            placeholder = item as PreferenceItem.Placeholder
                        )
                    }

                    "adaptive-icon-style" -> Preference(
                        item,
                        secondary = {
                            Column {
                                IconShapeSelector(
                                    names = stringArrayResource(id = R.array.adaptive_icon_style_names),
                                    pathMasks = stringArrayResource(id = R.array.adaptive_icon_style_paths_values),
                                    selected = screenState.widgetSettings.adaptiveIconStyle,
                                    defaultSystemMask = "",
                                    systemMaskName = "",
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth(),
                                    onPathChange = { newPath -> onEvent(WidgetCustomizeEvent.ApplyChange("adaptive-icon-style", newPath)) }
                                )
                            }
                        },
                        onClick = { })
                }
            }
        }
    }

    if (screenState.showBackgroundColorPicker) {
        ColorPickerSheet(
            title = stringResource(id = R.string.pref_bg_color_title),
            color = Color(screenState.widgetSettings.backgroundColor),
            showAlpha = true,
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("bg-color", it?.toArgb() ?: 0)) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowBackgroundColorPicker(show = false)) }
        )
    }

    if (screenState.showTileColorPicker) {
        ColorPickerSheet(
            title = stringResource(id = R.string.pref_tile_color_title),
            color = Color(screenState.widgetSettings.tileColor),
            showAlpha = true,
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("button-color", it?.toArgb() ?: 0)) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowTileColorPicker(show = false)) }
        )
    }

    if (screenState.showIconsColorPicker) {
        ColorPickerSheet(
            title = stringResource(id = R.string.pref_tint_color_title),
            color = screenState.widgetSettings.iconsColor?.let { Color(it) },
            showAlpha = false,
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("icons-color", it?.toArgb() ?: 0)) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowIconsColorPicker(show = false)) }
        )
    }

    if (screenState.showFontColorPicker) {
        ColorPickerSheet(
            title = stringResource(id = R.string.pref_font_color_title),
            color = screenState.widgetSettings.fontColor?.let { Color(it) },
            showAlpha = false,
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("font-color", it?.toArgb())) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowFontColorPicker(show = false)) }
        )
    }
}

@Composable
private fun ColorPickerSheet(title: String, color: Color?, showAlpha: Boolean, onColorChange: (Color?) -> Unit, onDismissRequest: () -> Unit) {
    BottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        ColorChooser(
            title = title,
            color = color,
            showAlpha = showAlpha,
            onColorChange = { onColorChange(it) },
        )
    }
}

@Composable
private fun ColorChooser(title: String, color: Color?, showAlpha: Boolean, onColorChange: (Color?) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp).align(Alignment.Start))
            ColorDialogContent(
                color = color,
                onColorChange = onColorChange,
                showNone = true,
                showAlpha = showAlpha,
                showSystemColors = true
            )
        }
    }
}

@Preview("WidgetLookMoreScreen Dark")
@Composable
fun WidgetLookMoreScreenDark() {
    val widgetSettings = WidgetInterface.NoOp()
    val skinViewFactory = DummySkinPreviewViewModel(LocalContext.current.applicationContext)

    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        WidgetCustomizeScreen(
            screenState = WidgetCustomizeState(
                items = createItems(widgetSettings, skinViewFactory.viewState.skinList),
                widgetSettings = widgetSettings,
                skinList = skinViewFactory.viewState.skinList,
            ),
            onEvent = { },
            skinViewFactory = skinViewFactory
        )
    }
}

@Preview
@Composable
fun ColorChooserPreview() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        ColorChooser(
            title = stringResource(id = R.string.pref_bg_color_title),
            color = null,
            showAlpha = true,
            onColorChange = {  },
        )
    }
}