package com.anod.car.home.incar

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.preferences.InCarSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class SwitchInCarActivity : Activity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getIntExtra(
            ModeService.EXTRA_MODE,
            if (ModeService.sInCarMode) ModeService.MODE_SWITCH_OFF else ModeService.MODE_SWITCH_ON
        )
        when (mode) {
            ModeService.MODE_SWITCH_OFF -> ModeService.stop(this)
            ModeService.MODE_SWITCH_ON -> if (get<InCarSettings>().isInCarEnabled) {
                ModeService.start(this, forceState = true)
                getKoin().get<BroadcastServiceManager>().registerBroadcastService()
            } else {
                Toast.makeText(this, info.anodsplace.carwidget.content.R.string.incar_mode_disabled, Toast.LENGTH_LONG).show()
            }
            else -> AppLog.e("Unknown in-car mode action: $mode")
        }
        finish()
    }
}