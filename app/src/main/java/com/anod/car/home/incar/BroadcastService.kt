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
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.extentions.isServiceRunning
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class BroadcastService : Service(), KoinComponent {

    class Manager(private val applicationContext: Context, private val inCarSettings: InCarSettings) : BroadcastServiceManager {
        override val isServiceRequired
            get() = Companion.isServiceRequired(inCarSettings)

        override val isServiceRunning: Boolean
            get() = applicationContext.isServiceRunning(BroadcastService::class.java)

        override fun registerBroadcastService() = Companion.registerBroadcastService(applicationContext, inCarSettings)

        override fun startService() = Companion.startService(applicationContext)

        override fun stopService() = Companion.stopService(applicationContext)
    }

    private var receiver: ModeBroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
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

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            AppLog.e(e)
            return START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        unregister(this)
        super.onDestroy()
    }

    private fun register(context: Context): Boolean {
        AppLog.i("Register BroadcastService")
        ModeDetector.onRegister(context)
        val prefs = get<InCarSettings>()
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
        val prefs = get<InCarSettings>()

        if (!prefs.isActivityRequired) {
            ActivityTransitionTracker(context).stop()
        }
    }

    companion object {

        private fun registerBroadcastService(context: Context, inCar: InCarInterface) {
            if (isServiceRequired(inCar)) {
                startService(context)
            } else {
                stopService(context)
            }
        }

        private fun startService(context: Context) {
            val service = Intent(context.applicationContext, BroadcastService::class.java)
            ContextCompat.startForegroundService(context, service)
        }

        private fun stopService(context: Context) {
            val service = Intent(context.applicationContext, BroadcastService::class.java)
            context.stopService(service)
        }

        private fun isServiceRequired(inCar: InCarInterface): Boolean {
            if (!inCar.isInCarEnabled) {
                return false
            }

            ModeDetector.updatePrefState(inCar)
            val states = ModeDetector.prefState

            for (i in states.indices) {
                if (states[i]) {
                    return true
                }
            }

            return false
        }
    }

}