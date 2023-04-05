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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.WidgetDialogType
import info.anodsplace.carwidget.screens.main.MainViewEvent
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
        suffixText = { Text(text = "px") }
    )
}

@Composable
fun WidgetCustomizeScreen(
    screenState: WidgetCustomizeState,
    onEvent: (WidgetCustomizeEvent) -> Unit,
    onMainViewEvent: (MainViewEvent) -> Unit,
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
                        "bg-color" -> onMainViewEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseBackgroundColor))
                        "icons-theme" -> onMainViewEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseIconsTheme))
                        "cmp-number" -> onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.value.toInt()))
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
            onMainViewEvent = { },
            skinViewFactory = skinViewFactory
        )
    }
}