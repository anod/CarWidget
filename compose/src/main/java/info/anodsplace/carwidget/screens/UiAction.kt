package info.anodsplace.carwidget.screens

sealed class UiAction {
    object None: UiAction()
    object OnBackNav: UiAction()
    class OpenWidgetConfig(val appWidgetId: Int): UiAction()
    class ApplyWidget(val appWidgetId: Int, val skinValue: String): UiAction()
}

sealed class WidgetActions : UiAction() {
    object ChooseShortcutsNumber: WidgetActions()
    object ChooseTileColor: WidgetActions()
    object ChooseBackgroundColor: WidgetActions()
    object ChooseIconsTheme: WidgetActions()
    class SwitchIconsMono(val isIconsMono: Boolean): WidgetActions()
    object ChooseIconsScale: WidgetActions()
}

