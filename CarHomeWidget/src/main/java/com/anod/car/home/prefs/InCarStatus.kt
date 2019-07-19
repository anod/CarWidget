package com.anod.car.home.prefs

import android.view.View
import android.widget.TextView
import com.anod.car.home.incar.ModeDetector
import com.anod.car.home.R

class InCarStatus(private val view: View) {

    companion object {
        val ids = listOf(
            R.id.status1,
            R.id.status2,
            R.id.status3,
            R.id.status4,
            R.id.status5
        )
        val titles = listOf(
            R.string.pref_power_connected_title,
            R.string.pref_headset_connected_title,
            R.string.pref_blutooth_device_title,
            R.string.activity_recognition,
            R.string.car_dock
        )

        val flags = listOf(
            ModeDetector.FLAG_POWER,
            ModeDetector.FLAG_HEADSET,
            ModeDetector.FLAG_BLUETOOTH,
            ModeDetector.FLAG_ACTIVITY,
            ModeDetector.FLAG_CAR_DOCK
        )
    }

    fun apply() {
        val events = ModeDetector.eventsState().sortedWith(compareBy({ !it.enabled }, { !it.active }))
        val resources = view.resources

        events.forEachIndexed { index, eventState ->
            val title = resources.getString(titles[eventState.id])
            val enabled = if (eventState.enabled) resources.getString(R.string.enabled) else resources.getString(R.string.disabled)
            val active = if (eventState.active) resources.getString(R.string.active) else resources.getString(R.string.not_active)
            view.findViewById<TextView>(ids[index]).text = String.format("%s - %s - %s", title, enabled, active)
        }
    }
}