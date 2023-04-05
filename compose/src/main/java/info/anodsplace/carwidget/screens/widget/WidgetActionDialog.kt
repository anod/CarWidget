package info.anodsplace.carwidget.screens.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.WidgetDialogEvent
import info.anodsplace.carwidget.screens.WidgetDialogType
import info.anodsplace.compose.ColorDialogContent

@Composable
fun WidgetActionDialogContent(current: WidgetDialogType, onEvent: (event: WidgetDialogEvent) -> Unit, dismiss: () -> Unit, widgetSettings: WidgetInterface.NoOp) {
    when (current) {
        WidgetDialogType.ChooseBackgroundColor -> ColorDialogContent(
            color = Color(widgetSettings.backgroundColor),
            //title = stringResource(id = R.string.color_picker_default_title),
            onColorChange = {
                onEvent(WidgetDialogEvent.UpdateBackgroundColor(it))
                dismiss()
            }
        )
        WidgetDialogType.ChooseIconsTheme -> { }
        WidgetDialogType.ChooseTileColor -> ColorDialogContent(
            color = Color(widgetSettings.tileColor),
            //title = stringResource(id = R.string.color_picker_default_title),
            onColorChange = {
                onEvent(WidgetDialogEvent.UpdateTileColor(it))
                dismiss()
            }
        )
        else -> {}
    }
}