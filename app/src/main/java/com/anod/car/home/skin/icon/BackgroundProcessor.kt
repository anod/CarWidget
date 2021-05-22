package com.anod.car.home.skin.icon

import info.anodsplace.carwidget.content.preferences.WidgetSettings

import android.graphics.Bitmap

/**
 * @author alex
 * @date 2014-12-06
 */
interface BackgroundProcessor {
    fun getColor(prefs: info.anodsplace.carwidget.content.preferences.WidgetSettings, icon: Bitmap?): Int
}
