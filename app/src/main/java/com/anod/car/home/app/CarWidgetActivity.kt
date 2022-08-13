package com.anod.car.home.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.CarWidgetApplication
import com.anod.car.home.R

/**
 * @author alex
 * @date 11/20/13
 */
abstract class CarWidgetActivity : AppCompatActivity() {

    val app: CarWidgetApplication
        get() = applicationContext as CarWidgetApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appThemeRes)
        AppCompatDelegate.setDefaultNightMode(app.appCompatNightMode)
        super.onCreate(savedInstanceState)
    }

    protected open val appThemeRes: Int
        get() = R.style.AppTheme

}