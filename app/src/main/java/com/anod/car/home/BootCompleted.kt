package com.anod.car.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anod.car.home.incar.BroadcastService

class BootCompleted : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        BroadcastService.registerBroadcastService(context)
    }
}