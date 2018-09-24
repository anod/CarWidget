package com.anod.car.home.incar

import android.app.Service
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import com.anod.car.home.notifications.ModeDetectorNotification

import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage
import info.anodsplace.framework.AppLog

class BroadcastService : Service() {

    private var receiver: ModeBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start once
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundNotification()
        }
        if (receiver != null) {
            return Service.START_STICKY
        }
        if (register(this)) {
            return Service.START_STICKY
        }
        stopForeground(true)
        stopSelf()
        return Service.START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        startForeground(ModeDetectorNotification.id, ModeDetectorNotification.create(this) )
    }

    override fun onDestroy() {
        unregister(this)
        super.onDestroy()
    }

    private fun register(context: Context): Boolean {
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
        return true
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
            val service = Intent(context.applicationContext, BroadcastService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service)
            } else {
                context.startService(service)
            }
        }

        fun stopService(context: Context) {
            val service = Intent(context.applicationContext, BroadcastService::class.java)
            context.stopService(service)
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (prefs.isEnableBluetoothOnPower || prefs.isDisableBluetoothOnPower) {
                    return true
                }
            }
            return false
        }
    }

}
