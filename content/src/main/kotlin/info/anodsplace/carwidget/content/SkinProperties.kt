package info.anodsplace.carwidget.content

import info.anodsplace.carwidget.content.graphics.IconProcessor

interface SkinProperties {
    val inCarButtonExitRes: Int
    val inCarButtonEnterRes: Int
    val setShortcutRes: Int
    val iconProcessor: IconProcessor?
    val backgroundProcessor: IconBackgroundProcessor?
    val setShortcutText: Int
    val iconPaddingRes: Int
    val settingsButtonRes: Int
    val rowLayout: Int
    val fontColorRes: Int
    fun getLayout(number: Int): Int
    fun hasWidgetButton1(): Boolean
}