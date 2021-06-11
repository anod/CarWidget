package com.anod.car.home.app

import com.anod.car.home.CarWidgetApplication
import com.anod.car.home.AppComponent

import android.content.Context

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
}
