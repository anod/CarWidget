package info.anodsplace.carwidget.screens

sealed interface UiAction {
    object None: UiAction
    object OnBackNav: UiAction
    class OpenWidgetConfig(val appWidgetId: Int): UiAction
    class ApplyWidget(val appWidgetId: Int, val skinValue: String): UiAction
    class ShowDialog(val type: WidgetDialog) : UiAction
}

sealed interface WidgetDialog {
    object None: WidgetDialog
    object ChooseShortcutsNumber: WidgetDialog
    object ChooseTileColor: WidgetDialog
    object ChooseBackgroundColor: WidgetDialog
    object ChooseIconsTheme: WidgetDialog
    class SwitchIconsMono(val isIconsMono: Boolean): WidgetDialog
    object ChooseIconsScale: WidgetDialog
}