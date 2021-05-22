package com.anod.car.home.incar

import android.app.Service
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.anod.car.home.notifications.ModeDetectorNotification

import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarStorage

class BroadcastService : Service() {

    private var receiver: ModeBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start once
        if (receiver == null) {
            startForeground(ModeDetectorNotification.id, ModeDetectorNotification.create(this))
            if (register(this)) {
                return START_STICKY
            }
        } else {
            startForeground(ModeDetectorNotification.id, ModeDetectorNotification.create(this))
            return START_STICKY
        }

        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregister(this)
        super.onDestroy()
    }

    private fun register(context: Context): Boolean {
        AppLog.i("Register BroadcastService")
        ModeDetector.onRegister(context)
        val prefs = InCarStorage.load(context)
        if (prefs.isActivityRequired) {
            AppLog.i("Start activity transition tracking")
            ActivityTransitionTracker(context).track()
        }

        if (!isServiceRequired(prefs)) {
            AppLog.i("Broadcast service is not required")
            return false
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_HEADSET_PLUG)
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        filter.addAction(Intent.ACTION_DOCK_EVENT)
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE)
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE)

        receiver = ModeBroadcastReceiver()
        context.registerReceiver(receiver, filter)
        return true
    }

    private fun unregister(context: Context) {
        AppLog.i("Unregister BroadcastService")
        if (receiver != null) {
            context.unregisterReceiver(receiver)
            receiver = null
        }
        val prefs = InCarStorage.load(context)

        if (!prefs.isActivityRequired) {
            ActivityTransitionTracker(context).stop()
        }
    }

    companion object {

        fun shouldStart(context: Context): Boolean {
            val isProOrTrial = Version(context).isProOrTrial
            return if (isProOrTrial) {
                val inCar = InCarStorage.load(context)
                inCar.isInCarEnabled && isServiceRequired(inCar)
            } else
                false
        }

        fun registerBroadcastService(context: Context) {
            if (shouldStart(context)) {
                startService(context)
            } else {
                stopService(context)
            }
        }

        fun startService(context: Context) {
            val service = Intent(context.applicationContext, BroadcastService::class.java)
            ContextCompat.startForegroundService(context, service)
        }

        fun stopService(context: Context) {
            val service = Intent(context.applicationContext, BroadcastService::class.java)
            context.stopService(service)
        }

        fun isServiceRequired(prefs: InCarInterface): Boolean {
            ModeDetector.updatePrefState(prefs)
            val states = ModeDetector.prefState

            for (i in states.indices) {
                if (states[i]) {
                    return true
                }
            }

            if (prefs.isEnableBluetoothOnPower || prefs.isDisableBluetoothOnPower) {
                return true
            }
            return false
        }
    }

}
