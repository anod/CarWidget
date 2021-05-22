package com.anod.car.home.skin.icon

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette
import info.anodsplace.carwidget.preferences.model.WidgetSettings

/**
 * @author alex
 * @date 2014-12-06
 */
class MetroBackgroundProcessor : BackgroundProcessor {

    override fun getColor(prefs: WidgetSettings, icon: Bitmap?): Int {
        if (prefs.paletteBackground) {
            if (icon == null) {
                return Color.DKGRAY
            }
            val palette = Palette.Builder(icon).generate()
            return palette.cardBackground
        }
        return prefs.tileColor!!
    }
}
