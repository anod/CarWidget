package com.anod.car.home.prefs

import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener

import com.anod.car.home.R
import com.anod.car.home.prefs.model.InCarStorage

class ConfigurationInCarVolume : ConfigurationPreferenceFragment(), OnCheckedChangeListener {

    override val isAppWidgetIdRequired: Boolean
        get() = false

    override val xmlResource: Int
        get() = R.xml.preference_incar_volume

    override val sharedPreferencesName: String
        get() = InCarStorage.PREF_NAME

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val prefs = InCarStorage.load(requireActivity())
        prefs.isAdjustVolumeLevel = isChecked
        prefs.apply()
    }

}
