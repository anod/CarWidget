package com.anod.car.home.skin.icon;

import com.anod.car.home.prefs.model.WidgetSettings;
import com.anod.car.home.prefs.preferences.Main;

import android.graphics.Bitmap;

/**
 * @author alex
 * @date 2014-12-06
 */
public class MetroBackgroundProcessor implements BackgroundProcessor {

    @Override
    public int getColor(WidgetSettings prefs, Bitmap icon) {
        return prefs.getTileColor();
    }
}
