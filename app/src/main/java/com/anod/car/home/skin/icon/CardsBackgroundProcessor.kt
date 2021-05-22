package com.anod.car.home.skin.icon

import info.anodsplace.carwidget.preferences.model.WidgetSettings

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette

/**
 * @author alex
 * @date 2014-12-06
 */
class CardsBackgroundProcessor : BackgroundProcessor {

    override fun getColor(prefs: WidgetSettings, icon: Bitmap?): Int {
        if (icon == null) {
            return Color.DKGRAY
        }
        val palette = Palette.Builder(icon).generate()
        return palette.cardBackground
    }
}
