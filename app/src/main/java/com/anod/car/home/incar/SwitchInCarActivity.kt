package com.anod.car.home.incar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.preferences.InCarSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class SwitchInCarActivity : Activity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isInCarEnabled = get<InCarSettings>().isInCarEnabled
        if (isInCarEnabled) {
            val service = Intent(this, ModeService::class.java)
            val data: Uri = if (ModeService.sInCarMode) {
                service.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF)
                service.putExtra(ModeService.EXTRA_FORCE_STATE, true)
                Uri.parse("com.anod.car.home://mode/0/2")
            } else {
                service.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON)
                service.putExtra(ModeService.EXTRA_FORCE_STATE, true)
                Uri.parse("com.anod.car.home://mode/1/2")
            }
            service.data = data
            startService(service)
        } else {
            Toast.makeText(this, info.anodsplace.carwidget.content.R.string.incar_mode_disabled, Toast.LENGTH_LONG).show()
//
//            val intent = ConfigurationActivity.createFragmentIntent(this, ConfigurationInCar::class.java)
//            startActivity(intent)
        }
        getKoin().get<BroadcastServiceManager>().registerBroadcastService()
        finish()
    }
}