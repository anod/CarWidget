package com.anod.car.home.incar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import info.anodsplace.framework.AppLog

class ModeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val act = intent.action ?: ""
        AppLog.i(" Action: $act")

        try {
            BroadcastService.startService(context)
            ModeDetector.onBroadcastReceive(context, intent)
        } catch (e: Exception) {
            AppLog.e(e)
        }
    }

}
