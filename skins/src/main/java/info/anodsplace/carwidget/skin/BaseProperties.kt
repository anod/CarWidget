package info.anodsplace.carwidget.skin

import android.content.Intent
import info.anodsplace.carwidget.content.IconBackgroundProcessor
import info.anodsplace.carwidget.content.InternalShortcutResources
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.graphics.IconProcessor

abstract class BaseProperties : SkinProperties {

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter

    override val setShortcutRes: Int
        get() = R.drawable.ic_add_shortcut

    override val iconProcessor: IconProcessor?
        get() = null

    override val setShortcutText: Int
        get() = info.anodsplace.carwidget.content.R.string.set_shortcut

    override val iconPaddingRes: Int
        get() = R.dimen.icon_padding_bottom

    override val fontColorRes: Int
        get() = R.color.default_font_color

    override val backgroundProcessor: IconBackgroundProcessor?
        get() = null

    override fun supportsWidgetButton1(): Boolean {
        return true
    }

    override val widgetButton1Id: Int
        get() = R.id.widget_btn1

    override val widgetButton2Id: Int
        get() = R.id.widget_btn2

    override val buttonAlternativeHiddenResId: Int
        get() = R.drawable.ic_action_cancel

    override val buttonTransparentResId: Int
        get() = R.drawable.btn_transparent

    override val containerId: Int
        get() = R.id.container

    override fun shortcutIdAt(position: Int): Int = btnIds[position]

    override fun shortcutTextIdAt(position: Int): Int = textIds[position]

    override fun iconResourceTint(iconResource: Intent.ShortcutIconResource?): Int = 0

    companion object {
        private val textIds = intArrayOf(
            R.id.btn_text0, R.id.btn_text1, //2
            R.id.btn_text2, R.id.btn_text3, //4
            R.id.btn_text4, R.id.btn_text5, //6
            R.id.btn_text6, R.id.btn_text7, //8
            R.id.btn_text8, R.id.btn_text9  //10
        )

        internal val btnIds = intArrayOf(
            R.id.btn0, R.id.btn1, //2
            R.id.btn2, R.id.btn3, //4
            R.id.btn4, R.id.btn5, //6
            R.id.btn6, R.id.btn7, //8
            R.id.btn8, R.id.btn9  //10
        )

        val internalShortcutResourcesPrimary = InternalShortcutResources(
                icons = listOf(
                        R.drawable.ic_launcher_carwidget,
                        R.drawable.ic_shortcut_call_primary,
                        R.drawable.ic_shortcut_play_primary,
                        R.drawable.ic_shortcut_next_primary,
                        R.drawable.ic_shortcut_previous_primary
                )
        )
    }
}