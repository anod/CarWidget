package info.anodsplace.carwidget.screens.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.WidgetActions
import info.anodsplace.compose.ColorDialog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun WidgetActionDialog(modifier: Modifier, current: UiAction, action: MutableSharedFlow<UiAction>, widgetSettings: WidgetInterface) {
    when (current) {
        WidgetActions.ChooseBackgroundColor -> BackgroundColor(widgetSettings, action)
        WidgetActions.ChooseIconsScale -> IconScaleDialog(widgetSettings, action)
        WidgetActions.ChooseIconsTheme -> { }
        WidgetActions.ChooseShortcutsNumber -> ShortcutNumbersDialog(modifier, widgetSettings, action)
        WidgetActions.ChooseTileColor -> { }
        is WidgetActions.SwitchIconsMono -> { }
        else -> {}
    }
}

@Composable
fun IconScaleDialog(prefs: WidgetInterface, action: MutableSharedFlow<UiAction>) {
    val coroutineScope = rememberCoroutineScope()

    Dialog(
            onDismissRequest = {
                coroutineScope.launch { action.emit(UiAction.None) }
            },
            properties = DialogProperties()
    ) {
        Surface {
            IconScale(prefs, action)
        }
    }
}

@Composable
fun IconScale(prefs: WidgetInterface, action: MutableSharedFlow<UiAction>) {
    (0..20)
}

@Composable
fun BackgroundColor(prefs: WidgetInterface, action: MutableSharedFlow<UiAction>) {
    val selected = Color(prefs.backgroundColor)
    val coroutineScope = rememberCoroutineScope()
    ColorDialog(selected = selected) { newColor ->
        if (newColor != null) {
            prefs.backgroundColor = newColor.value.toInt()
        }
        coroutineScope.launch { action.emit(UiAction.None) }
    }
}

@Composable
fun ShortcutNumbersDialog(modifier: Modifier, widgetSettings: WidgetInterface, action: MutableSharedFlow<UiAction>) {
    val coroutineScope = rememberCoroutineScope()

    Dialog(
            onDismissRequest = {
                coroutineScope.launch { action.emit(UiAction.None) }
            },
            properties = DialogProperties()
    ) {
        Surface {
            val current = widgetSettings.shortcutsNumber
            ShortcutsNumbers(current, onClick = {
                widgetSettings.shortcutsNumber = it
                coroutineScope.launch { action.emit(UiAction.None) }
            })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortcutsNumbers(current: Int, onClick: (Int) -> Unit) {

    FlowRow(
            modifier = Modifier.padding(16.dp),
            mainAxisAlignment = MainAxisAlignment.Start
    ) {
        ShortcutsNumber(number = 4, current = current, boxSize = 32.dp, onClick = onClick)
        ShortcutsNumber(number = 6, current = current, boxSize = 20.dp, onClick = onClick)
        ShortcutsNumber(number = 8, current = current, boxSize = 16.dp, onClick = onClick)
        ShortcutsNumber(number = 10, current = current, boxSize = 12.dp, onClick = onClick)
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
                        .size(size = 80.dp)
                        .border(
                            width = if (current == number) 2.dp else 0.dp,
                            color = MaterialTheme.colors.primaryVariant,
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
                                    color = MaterialTheme.colors.primaryVariant,
                                    shape = RoundedCornerShape(4.dp)
                            )) {
                    }
                    Box(modifier = Modifier
                            .size(boxSize)
                            .padding(start = 1.dp)
                            .background(
                                    color = MaterialTheme.colors.primaryVariant,
                                    shape = RoundedCornerShape(4.dp)
                            ))
                }
            }
        }
        Text(
                modifier = Modifier.padding(8.dp),
                text = number.toString(),
                color = Color.White,
                textAlign = TextAlign.Start,
                fontSize = 22.sp
        )
    }
}

@Preview
@Composable
fun ShortcutNumbersPreview() {
    CarWidgetTheme() {
        Surface(modifier = Modifier.width(256.dp)) {
            ShortcutsNumbers(8, onClick = { })
        }
    }
}