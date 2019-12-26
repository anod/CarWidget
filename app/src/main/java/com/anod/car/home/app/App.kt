package com.anod.car.home.app

import com.anod.car.home.CarWidgetApplication
import com.anod.car.home.AppComponent

import android.content.Context
import com.anod.car.home.prefs.model.AppTheme

/**
 * @author alex
 * @date 2015-07-05
 */
object App {

    fun get(context: Context): CarWidgetApplication {
        return context.applicationContext as CarWidgetApplication
    }

    fun provide(context: Context): AppComponent {
        return (context.applicationContext as CarWidgetApplication).appComponent
    }

    fun theme(context: Context): AppTheme {
        return provide(context).theme
    }
}
