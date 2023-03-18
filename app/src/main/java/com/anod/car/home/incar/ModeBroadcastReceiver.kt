package com.anod.car.home.incar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.BroadcastServiceManager
import org.koin.core.component.KoinComponent

class ModeBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        val act = intent.action ?: ""
        AppLog.i(" Action: $act")

        try {
            getKoin().get<BroadcastServiceManager>().startService()
            ModeDetector.onBroadcastReceive(context, intent)
        } catch (e: Exception) {
            AppLog.e(e)
        }
    }

}
