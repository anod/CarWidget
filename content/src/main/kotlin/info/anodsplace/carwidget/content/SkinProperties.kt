package info.anodsplace.carwidget.content

import android.content.Intent
import android.content.res.ColorStateList
import info.anodsplace.carwidget.content.db.ShortcutIconConverter
import info.anodsplace.carwidget.content.graphics.IconProcessor

data class InternalShortcutResources(
        val icons: List<Int>
)

interface SkinProperties {
    val name: String
    val iconConverter: ShortcutIconConverter?
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
    fun iconResourceTint(iconResource: Intent.ShortcutIconResource?): ColorStateList?
}