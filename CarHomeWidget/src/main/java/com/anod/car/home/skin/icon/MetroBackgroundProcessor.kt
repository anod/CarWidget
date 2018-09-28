package com.anod.car.home.skin.icon

import android.graphics.Bitmap
import com.anod.car.home.prefs.model.WidgetSettings

/**
 * @author alex
 * @date 2014-12-06
 */
class MetroBackgroundProcessor : BackgroundProcessor {

    override fun getColor(prefs: WidgetSettings, icon: Bitmap?): Int {
        return prefs.tileColor!!
    }
}
