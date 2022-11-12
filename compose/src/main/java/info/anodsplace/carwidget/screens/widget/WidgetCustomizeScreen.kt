package info.anodsplace.carwidget.screens.widget

import android.app.UiModeManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
        suffixText = { Text(text = "px")}
    )
}

@Composable
fun WidgetCustomizeScreen(
    screenState: WidgetCustomizeState,
    onEvent: (WidgetCustomizeEvent) -> Unit,
    onMainViewEvent: (MainViewEvent) -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    Surface {
        PreferencesScreen(
            modifier = Modifier.padding(innerPadding),
            preferences = screenState.items,
            onClick = { item ->
                when (item) {
                    is PreferenceItem.CheckBox -> {
                        onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.checked))
                    }
                    is PreferenceItem.Switch -> {
                        onEvent(WidgetCustomizeEvent.ApplyChange(item.key, item.checked))
                    }
                    else -> {
                        when (item.key) {
                            "bg-color" -> onMainViewEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseBackgroundColor))
                            "icons-theme" -> onMainViewEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseIconsTheme))
                            "icons-scale" -> onMainViewEvent(MainViewEvent.ShowDialog(WidgetDialogType.ChooseIconsScale))
                        }
                    }
                }
            }
        ) { item, paddingValues ->
            when (item.key) {
                "cmp-number" -> Preference(
                    item,
                    secondary = {
                        ShortcutsNumbers(screenState.widgetSettings.shortcutsNumber, onClick = {
                            onEvent(WidgetCustomizeEvent.ApplyChange("cmp-number", it))
                        })
                    },
                    onClick = { })
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
                                names =  stringArrayResource(id = R.array.adaptive_icon_style_names),
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


@Composable
fun ShortcutsNumbers(current: Int, onClick: (Int) -> Unit) {
    val numbers = listOf(4,6,8,10)
    val boxSizes = listOf(40.dp,28.dp,22.dp,18.dp)
    LazyRow {
        items(numbers.size) { index ->
            val number = numbers[index]
            val boxSize = boxSizes[index]
            ShortcutsNumber(number = number, current = current, boxSize = boxSize, onClick = onClick)
        }
    }
}

@Composable
fun ShortcutsNumber(number: Int, current: Int, boxSize: Dp, onClick: (Int) -> Unit) {
    val rowsNumber = number / 2
    Box(modifier = Modifier
        .padding(4.dp)
        .clickable { onClick(number) }) {
        Column(
            modifier = Modifier
                .size(size = 96.dp)
                .border(
                    width = if (current == number) 2.dp else 1.dp,
                    color = if (current == number) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.38f
                    ),
                    shape = RoundedCornerShape(4.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in (0 until rowsNumber)) {
                Row(modifier = Modifier.padding(top = 1.dp)) {
                    Box(modifier = Modifier
                        .size(boxSize)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )) {
                    }
                    Box(modifier = Modifier
                        .size(boxSize)
                        .padding(start = 1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ))
                }
            }
        }
        Text(
            modifier = Modifier.padding(8.dp),
            text = number.toString(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Preview("WidgetLookMoreScreen Dark")
@Composable
fun WidgetLookMoreScreenDark() {
    val widgetSettings = WidgetInterface.NoOp()
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        WidgetCustomizeScreen(
            screenState = WidgetCustomizeState(
                items = createItems(widgetSettings),
                widgetSettings = widgetSettings
            ),
            onEvent = { },
            onMainViewEvent = { }
        )
    }
}