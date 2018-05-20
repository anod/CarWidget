package com.anod.car.home.app

import com.anod.car.home.CarWidgetApplication
import com.anod.car.home.prefs.model.AppTheme

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate

/**
 * @author alex
 * @date 11/20/13
 */
abstract class CarWidgetActivity : AppCompatActivity() {

    protected open val isTransparentAppTheme: Boolean
        get() = false

    val app: CarWidgetApplication
        get() = App.get(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = app.themeIdx
        setTheme(getAppThemeRes(theme))
        AppCompatDelegate.setDefaultNightMode(app.nightMode)
        super.onCreate(savedInstanceState)

    }

    protected open fun getAppThemeRes(theme: Int): Int {
        return if (isTransparentAppTheme)
            AppTheme.getTransparentResource(theme)
        else
            AppTheme.getMainResource(theme)
    }

}
