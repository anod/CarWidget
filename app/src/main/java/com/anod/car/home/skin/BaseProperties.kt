package com.anod.car.home.skin

import com.anod.car.home.R
import info.anodsplace.carwidget.content.IconBackgroundProcessor
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
        get() = R.string.set_shortcut

    override val iconPaddingRes: Int
        get() = R.dimen.icon_padding_bottom

    override val fontColorRes: Int
        get() = R.color.default_font_color

    override val backgroundProcessor: IconBackgroundProcessor?
        get() = null

    override fun hasWidgetButton1(): Boolean {
        return true
    }
}