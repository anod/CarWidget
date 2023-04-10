package info.anodsplace.carwidget.screens.widget

import android.app.UiModeManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.TextDecreaseIcon
import info.anodsplace.carwidget.TextIncreaseIcon
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
                                    names = stringArrayResource(id = info.anodsplace.carwidget.content.R.array.adaptive_icon_style_names),
                                    pathMasks = stringArrayResource(id = info.anodsplace.carwidget.content.R.array.adaptive_icon_style_paths_values),
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
            color = Color(screenState.widgetSettings.backgroundColor),
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("bg-color", it)) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowBackgroundColorPicker(show = false)) }
        )
    }

    if (screenState.showTileColorPicker) {
        ColorPickerSheet(
            color = Color(screenState.widgetSettings.tileColor),
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("button-color", it)) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowTileColorPicker(show = false)) }
        )
    }

    if (screenState.showFontColorPicker) {
        ColorPickerSheet(
            color = screenState.widgetSettings.fontColor?.let { Color(it) },
            onColorChange = { onEvent(WidgetCustomizeEvent.ApplyChange("font-color", it)) },
            onDismissRequest = { onEvent(WidgetCustomizeEvent.ShowFontColorPicker(show = false)) }
        )
    }
}

@Composable
private fun ColorPickerSheet(color: Color?, onColorChange: (Color?) -> Unit, onDismissRequest: () -> Unit) {
    BottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        ColorChooser(
            color = color,
            onColorChange = { onColorChange(it) },
        )
    }
}

@Composable
private fun ColorChooser(color: Color?, onColorChange: (Color?) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        ColorDialogContent(
            color = color,
            onColorChange = onColorChange,
            showNone = true,
            showAlpha = true
        )
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