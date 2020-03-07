package com.anod.car.home.prefs.model

import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import com.anod.car.home.R

/**
 * @author algavris
 * @date 09/04/2016.
 */
class AppTheme(val themeIdx: Int) {

    companion object {
        const val gray = 0
        const val dark = 1
    }

    val main: Int
        @StyleRes get() = R.style.AppTheme

    val mainResource: Int
        @StyleRes get() = R.style.AppTheme

    val transparentResource: Int
        @StyleRes get() = R.style.AppTheme_Transparent

    val noActionBarResource: Int
        @StyleRes get() = R.style.AppTheme_Transparent

    val backgroundResource: Int
        @ColorRes get() =  if (themeIdx == gray) R.color.panel_background_grey else R.color.panel_background_dark

    val alert: Int
        @StyleRes get() = R.style.Alert

    val dialog: Int
        @StyleRes get() = R.style.Dialog

}
