package info.anodsplace.carwidget.screens

sealed class UiAction {
    object OnBackNav: UiAction()
    open class IntentEditAction: UiAction()
    class OpenWidgetConfig(val appWidgetId: Int): UiAction()
    class ApplyWidget(val appWidgetId: Int, val skinValue: String): UiAction()
}