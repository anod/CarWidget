package com.anod.car.home.incar

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

import com.anod.car.home.app.StoppableService
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage
import info.anodsplace.framework.AppLog

class BroadcastService : StoppableService() {

    private var receiver: ModeBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onAfterStart(intent: Intent) {
        register(this)
    }

    override fun onBeforeStop(intent: Intent) {
        unregister(this)
    }

    private fun register(context: Context) {
        AppLog.d("BroadcastService::register")
        if (receiver == null) {

            ModeDetector.onRegister(context)
            val prefs = InCarStorage.load(context)
            if (prefs.isActivityRequired) {
                AppLog.d("ActivityRecognitionClientService started")
                ActivityRecognitionClientService.startService(context)
            }

            if (!isServiceRequired(prefs)) {
                AppLog.d("Broadcast service is not required")
                stopSelf()
                return
            }

            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_HEADSET_PLUG)
            receiver = ModeBroadcastReceiver.create()
            context.registerReceiver(receiver, filter)
        }
    }

    private fun unregister(context: Context) {
        AppLog.d("BroadcastService::unregister")
        if (receiver != null) {
            context.unregisterReceiver(receiver)
            receiver = null
        }
        val prefs = InCarStorage.load(context)

        if (!prefs.isActivityRequired) {
            ActivityRecognitionClientService.stopService(context)
        }
    }

    companion object {

        fun startService(context: Context) {
            val updateIntent = Intent(context.applicationContext, BroadcastService::class.java)
            context.startService(updateIntent)
        }

        fun stopService(context: Context) {
            val receiverIntent = Intent(context.applicationContext, BroadcastService::class.java)
            StoppableService.Companion.fillStopIntent(receiverIntent)
            context.startService(receiverIntent)
        }

        fun isServiceRequired(prefs: InCarInterface): Boolean {
            ModeDetector.updatePrefState(prefs)
            val states = ModeDetector.prefState

            for (i in states.indices) {
                if (i == ModeDetector.FLAG_ACTIVITY) {
                    continue
                }
                if (states[i]) {
                    return true
                }
            }
            return false
        }
    }


}
