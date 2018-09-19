package com.anod.car.home.app

import com.anod.car.home.CarWidgetApplication
import com.anod.car.home.prefs.model.AppTheme

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * @author alex
 * @date 11/20/13
 */
abstract class CarWidgetActivity : AppCompatActivity() {

    val app: CarWidgetApplication
        get() = App.get(this)

    val theme: AppTheme
        get() = App.theme(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appThemeRes)
        AppCompatDelegate.setDefaultNightMode(app.nightMode)
        super.onCreate(savedInstanceState)

    }

    protected open val appThemeRes: Int
        get() = theme.mainResource

}
