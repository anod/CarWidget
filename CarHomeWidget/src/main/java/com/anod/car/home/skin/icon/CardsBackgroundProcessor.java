package com.anod.car.home.skin.icon;

import com.anod.car.home.prefs.model.WidgetSettings;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.palette.graphics.Palette;

/**
 * @author alex
 * @date 2014-12-06
 */
public class CardsBackgroundProcessor implements BackgroundProcessor {

    @Override
    public int getColor(WidgetSettings prefs, Bitmap icon) {
        if (icon == null) {
            return Color.DKGRAY;
        }
        Palette palette = new Palette.Builder(icon).generate();

        Palette.Swatch swatch = palette.getMutedSwatch();

        if (swatch != null) {
            return swatch.getRgb();
        }
        swatch = palette.getDarkVibrantSwatch();
        if (swatch != null) {
            return swatch.getRgb();
        }
        swatch = palette.getVibrantSwatch();
        if (swatch != null) {
            return swatch.getRgb();
        }
        return Color.DKGRAY;
    }
}
