package com.anod.car.home.skin.icon;

import android.graphics.Bitmap;

import com.anod.car.home.prefs.preferences.Main;

/**
 * @author alex
 * @date 2014-12-06
 */
public class MetroBackgroundProcessor implements BackgroundProcessor {
    @Override
    public int getColor(Main prefs, Bitmap icon) {
        return prefs.getTileColor();
    }
}
