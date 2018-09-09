package com.anod.car.home.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.anod.car.home.CarWidgetApplication

/**
 * @author algavris
 * @date 20/05/2018
 */
open class AppViewModel(application: Application) : AndroidViewModel(application) {

    val app: CarWidgetApplication
        get() = getApplication()

}