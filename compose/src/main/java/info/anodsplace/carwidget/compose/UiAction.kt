package info.anodsplace.carwidget.compose

sealed class UiAction {
    object OnBackNav: UiAction()
    open class IntentEditAction: UiAction()
}