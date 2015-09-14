package com.anod.car.home.skin.icon;

import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.utils.AppLog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;

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
        Palette palette = new Palette.Builder(icon).generate();

        Palette.Swatch swatch = palette.getMutedSwatch();
//        AppLog.d("MutedSwatch: "+(swatch == null?"null":"#"+Integer.toHexString(swatch.getRgb())));
//        swatch = palette.getVibrantSwatch();
//        AppLog.d("VibrantSwatch: "+(swatch == null?"null":"#"+Integer.toHexString(swatch.getRgb())));
//        swatch = palette.getDarkMutedSwatch();
//        AppLog.d("DarkMutedSwatch: "+(swatch == null?"null":"#"+Integer.toHexString(swatch.getRgb())));
//        swatch = palette.getDarkVibrantSwatch();
//        AppLog.d("DarkVibrantSwatch: "+(swatch == null?"null":"#"+Integer.toHexString(swatch.getRgb())));

        if (swatch != null) {
            AppLog.d("MutedSwatch: #" + Integer.toHexString(swatch.getRgb()));
            return swatch.getRgb();
        }
        swatch = palette.getDarkVibrantSwatch();
        if (swatch != null) {
            AppLog.d("DarkVibrantSwatch: #" + Integer.toHexString(swatch.getRgb()));
            return swatch.getRgb();
        }
        swatch = palette.getVibrantSwatch();
        if (swatch != null) {
            AppLog.d("VibrantSwatch: #" + Integer.toHexString(swatch.getRgb()));
            return swatch.getRgb();
        }
        return Color.DKGRAY;
    }
}
