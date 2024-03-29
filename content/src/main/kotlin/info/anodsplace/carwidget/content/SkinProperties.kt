package info.anodsplace.carwidget.content

import android.content.Intent
import info.anodsplace.carwidget.content.graphics.IconProcessor

data class InternalShortcutResources(
        val icons: List<Int>
)

interface SkinProperties {

    interface Factory {
        fun create(skinName: String): SkinProperties
    }

    val name: String
    val buttonTransparentResId: Int
    val buttonAlternativeHiddenResId: Int
    val widgetButton1Id: Int
    val widgetButton2Id: Int
    val containerId: Int
    val backgroundId: Int
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
    fun supportsWidgetButton1(): Boolean
    fun shortcutIdAt(position: Int): Int
    fun shortcutTextIdAt(position: Int): Int
    fun iconResourceTint(iconResource: Intent.ShortcutIconResource?): Int
}