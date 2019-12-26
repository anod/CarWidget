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
        @StyleRes get() = R.style.AppThemeGray

    val mainResource: Int
        @StyleRes get() = if (themeIdx == gray) R.style.AppThemeGray else R.style.AppThemeHolo

    val transparentResource: Int
        @StyleRes get() = if (themeIdx == gray) R.style.AppThemeGray_Transparent else R.style.AppThemeHolo_Transparent

    val noActionBarResource: Int
        @StyleRes get() = if (themeIdx == gray) R.style.AppThemeGray_NoActionBar else R.style.AppThemeHolo_NoActionBar

    val backgroundResource: Int
        @ColorRes get() =  if (themeIdx == gray) R.color.panel_background_grey else R.color.panel_background_dark

    val alert: Int
        @StyleRes get() = if (themeIdx == gray) R.style.AlertGray else R.style.AlertHolo

    val dialog: Int
        @StyleRes get() = if (themeIdx == gray) R.style.DialogGrey else R.style.DialogHolo

}
