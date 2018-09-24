package com.anod.car.home.skin.icon

import com.anod.car.home.prefs.model.WidgetSettings

import android.graphics.Bitmap

/**
 * @author alex
 * @date 2014-12-06
 */
interface BackgroundProcessor {
    fun getColor(prefs: WidgetSettings, icon: Bitmap?): Int
}
