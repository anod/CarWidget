package com.anod.car.home.skin.icon

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette
import info.anodsplace.carwidget.content.IconBackgroundProcessor
import info.anodsplace.carwidget.content.preferences.WidgetInterface

/**
 * @author alex
 * @date 2014-12-06
 */
class CardsBackgroundProcessor : IconBackgroundProcessor {

    override fun getColor(prefs: WidgetInterface, icon: Bitmap?): Int {
        if (icon == null) {
            return Color.DKGRAY
        }
        val palette = Palette.Builder(icon).generate()
        return palette.cardBackground
    }
}