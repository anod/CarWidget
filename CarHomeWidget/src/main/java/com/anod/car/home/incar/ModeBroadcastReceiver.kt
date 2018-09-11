package com.anod.car.home.incar

import info.anodsplace.framework.AppLog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ModeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val act = intent.action
        AppLog.d(" Action: " + act!!)

        BroadcastService.startService(context)

        ModeDetector.onBroadcastReceive(context, intent)
    }

    companion object {
        const val ACTION_ACTIVITY_RECOGNITION = "com.anod.car.home.incar.ACTION_ACTIVITY_RECOGNITION"
        const val EXTRA_ACTIVITY_RESULT = "extra_activity"
    }

}
