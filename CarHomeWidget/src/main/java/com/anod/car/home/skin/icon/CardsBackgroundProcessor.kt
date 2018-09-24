package com.anod.car.home.skin.icon

import com.anod.car.home.prefs.model.WidgetSettings

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

        var swatch: Palette.Swatch? = palette.mutedSwatch

        if (swatch != null) {
            return swatch.rgb
        }
        swatch = palette.darkVibrantSwatch
        if (swatch != null) {
            return swatch.rgb
        }
        swatch = palette.vibrantSwatch
        return swatch?.rgb ?: Color.DKGRAY
    }
}
