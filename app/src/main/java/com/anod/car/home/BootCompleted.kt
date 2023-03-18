package com.anod.car.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.BroadcastServiceManager
import org.koin.core.component.KoinComponent

class BootCompleted : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            getKoin().get<BroadcastServiceManager>().registerBroadcastService()
        }
    }
}