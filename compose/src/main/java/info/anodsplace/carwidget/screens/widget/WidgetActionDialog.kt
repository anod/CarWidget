package info.anodsplace.carwidget.screens.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        WidgetActions.ChooseIconsScale -> { }
        WidgetActions.ChooseIconsTheme -> { }
        WidgetActions.ChooseShortcutsNumber -> { }
        WidgetActions.ChooseTileColor -> { }
        is WidgetActions.SwitchIconsMono -> { }
        else -> {}
    }
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