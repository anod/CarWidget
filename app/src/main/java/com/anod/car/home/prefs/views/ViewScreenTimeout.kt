package com.anod.car.home.prefs.views

import android.view.View
import android.widget.CheckBox
import android.widget.Switch
import com.anod.car.home.R

class ViewScreenTimeout(private val view: View) {

    private var listener: (keepOn: Boolean, whileCharging: Boolean, useAlert: Boolean) -> Unit = { _, _, _ -> }

    val screenOnSwitch: Switch
        get() = view.findViewById(R.id.screenOnSwitch)

    val whileCharging: CheckBox
        get() = view.findViewById(R.id.whileCharging)

    val useAlert: CheckBox
        get() = view.findViewById(R.id.useAlert)

    val useAlertGroup: View
        get() = view.findViewById(R.id.useAlertGroup)

    init {
        screenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            whileCharging.isEnabled = isChecked
            useAlert.isEnabled = isChecked
            this.listener(isChecked, whileCharging.isChecked, useAlert.isChecked)
        }

        whileCharging.setOnCheckedChangeListener { _, isChecked ->
            this.listener(screenOnSwitch.isChecked, isChecked, useAlert.isChecked)
        }

        useAlert.setOnCheckedChangeListener { _, isChecked ->
            this.listener(screenOnSwitch.isChecked, whileCharging.isChecked, isChecked)
        }
    }

    fun onStateChange(listener: (keepOn: Boolean, whileCharging: Boolean, useAlert: Boolean) -> Unit) {
        this.listener = listener
    }
}