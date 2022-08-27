package info.anodsplace.carwidget.screens.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.WidgetDialogType
import info.anodsplace.compose.ColorDialogContent

@Composable
fun WidgetActionDialogContent(current: WidgetDialogType, onEvent: (event: SkinPreviewViewEvent) -> Unit, dismiss: () -> Unit, widgetSettings: WidgetInterface.NoOp) {
    when (current) {
        WidgetDialogType.ChooseBackgroundColor -> ColorDialogContent(
            color = Color(widgetSettings.backgroundColor),
            title = stringResource(id = R.string.color_picker_default_title),
            onColorChange = {
                onEvent(SkinPreviewViewEvent.UpdateBackgroundColor(it))
                dismiss()
            }
        )
        WidgetDialogType.ChooseIconsScale -> IconScaleDialogContent(widgetSettings, dismiss = dismiss, onEvent = onEvent)
        WidgetDialogType.ChooseIconsTheme -> { }
        WidgetDialogType.ChooseShortcutsNumber -> ShortcutNumbersDialogContent(widgetSettings, dismiss = dismiss, onEvent = onEvent)
        WidgetDialogType.ChooseTileColor -> ColorDialogContent(
            color = Color(widgetSettings.tileColor),
            title = stringResource(id = R.string.color_picker_default_title),
            onColorChange = {
                onEvent(SkinPreviewViewEvent.UpdateTileColor(it))
                dismiss()
            }
        )
        else -> {}
    }
}

@Composable
fun IconScaleDialogContent(prefs: WidgetInterface.NoOp, dismiss: () -> Unit = { }, onEvent: (event: SkinPreviewViewEvent) -> Unit = { }) {
    Surface {
        Column(
            modifier = Modifier
                .padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.pref_scale_icon),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                textAlign = TextAlign.Center
            )
            val current = prefs.iconsScale.toInt()
            IconScale(current) { scale ->
                onEvent(SkinPreviewViewEvent.UpdateIconScale(scale.toString()))
                dismiss()
            }
        }
    }
}

@Composable
fun IconScale(current: Int, onClick: (Int) -> Unit) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start
    ) {
        repeat((0..20).count()) { scale ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onClick(scale) }
                    .size(size = 64.dp)
                    .border(
                        width = if (current == scale) 2.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val scaleText = "x%.1f".format(WidgetInterface.convertIconsScale(scale = scale))
                Text(text = scaleText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun ShortcutNumbersDialogContent(widgetSettings: WidgetInterface.NoOp, dismiss: () -> Unit = { }, onEvent: (event: SkinPreviewViewEvent) -> Unit = { }) {
    val current = widgetSettings.shortcutsNumber
    Surface {
        Column(
            modifier = Modifier
                .padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.number_shortcuts_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                textAlign = TextAlign.Center
            )
            ShortcutsNumbers(current, onClick = {
                onEvent(SkinPreviewViewEvent.UpdateShortcutsNumber(it))
                dismiss()
            })
        }
    }
}

@Composable
fun ShortcutsNumbers(current: Int, onClick: (Int) -> Unit) {
    FlowRow(
            mainAxisAlignment = MainAxisAlignment.Start
    ) {
        ShortcutsNumber(number = 4, current = current, boxSize = 40.dp, onClick = onClick)
        ShortcutsNumber(number = 6, current = current, boxSize = 28.dp, onClick = onClick)
        ShortcutsNumber(number = 8, current = current, boxSize = 22.dp, onClick = onClick)
        ShortcutsNumber(number = 10, current = current, boxSize = 18.dp, onClick = onClick)
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
                        color = if (current == number) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
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

@Preview
@Composable
fun ShortcutNumbersPreview() {
    CarWidgetTheme {
        ShortcutNumbersDialogContent(WidgetInterface.NoOp())
    }
}

@Preview
@Composable
fun IconScalePreview() {
    CarWidgetTheme {
        IconScaleDialogContent(WidgetInterface.NoOp())
    }
}