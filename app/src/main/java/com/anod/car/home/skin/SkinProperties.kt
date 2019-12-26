package com.anod.car.home.skin


import com.anod.car.home.skin.icon.BackgroundProcessor
import com.anod.car.home.skin.icon.IconProcessor

interface SkinProperties {
    val inCarButtonExitRes: Int
    val inCarButtonEnterRes: Int
    val setShortcutRes: Int
    val iconProcessor: IconProcessor?
    val backgroundProcessor: BackgroundProcessor?
    val setShortcutText: Int
    val iconPaddingRes: Int
    val settingsButtonRes: Int
    val rowLayout: Int
    fun getLayout(number: Int): Int
    fun hasWidgetButton1(): Boolean
}
