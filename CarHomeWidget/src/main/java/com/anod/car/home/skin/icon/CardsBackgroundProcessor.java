package com.anod.car.home.skin.icon;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;

import com.anod.car.home.prefs.preferences.Main;

/**
 * @author alex
 * @date 2014-12-06
 */
public class CardsBackgroundProcessor implements BackgroundProcessor {

    @Override
    public int getColor(Main prefs, Bitmap icon) {
        if (icon == null) {
            return Color.DKGRAY;
        }
        Palette palette = Palette.generate(icon);
        Palette.Swatch swatch;

        swatch = palette.getDarkVibrantSwatch();
        if (swatch == null) {
            swatch = palette.getDarkVibrantSwatch();
        }
        if (swatch == null) {
            swatch = palette.getVibrantSwatch();
        }
        if (swatch == null) {
            swatch = palette.getMutedSwatch();
        }
        if (swatch == null) {
            return Color.DKGRAY;
        }
        return swatch.getRgb();
    }
}
