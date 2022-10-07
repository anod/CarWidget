package info.anodsplace.carwidget.content

import info.anodsplace.carwidget.content.graphics.IconProcessor

class InternalShortcutResources(
        val icons: IntArray
)

interface SkinProperties {
    val internalShortcuts: InternalShortcutResources
    val buttonTransparentResId: Int
    val buttonAlternativeHiddenResId: Int
    val widgetButton1Id: Int
    val widgetButton2Id: Int
    val containerId: Int
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
    fun shortcutIdAt(position: Int): Int
    fun shortcutTextIdAt(position: Int): Int
}