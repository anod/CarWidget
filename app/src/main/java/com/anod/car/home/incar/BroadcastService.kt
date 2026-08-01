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
import org.koin.core.context.GlobalContext

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
        // Enter the foreground immediately and unconditionally, before any other work, so we never
        // miss the start deadline (RemoteServiceException$ForegroundServiceDidNotStartInTimeException).
        try {
            startForeground(ModeDetectorNotification.id, ModeDetectorNotification.create(this))
        } catch (e: Exception) {
            AppLog.e(e)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            if (receiver != null || register(this)) {
                // START_NOT_STICKY on purpose: allowing the OS to auto-restart this foreground
                // service from the background (START_STICKY) leads to repeated foreground-start
                // timeout crashes on API 31+. It is re-started on demand by ModeBroadcastReceiver
                // and settings changes when actually required.
                return START_NOT_STICKY
            }
        } catch (e: Exception) {
            AppLog.e(e)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
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

        // Assign the field only after registration succeeds so a failed registerReceiver()
        // does not leave a non-null-but-unregistered receiver that later crashes unregister().
        val modeReceiver = ModeBroadcastReceiver()
        context.registerReceiver(modeReceiver, filter)
        receiver = modeReceiver
        return true
    }

    private fun unregister(context: Context) {
        AppLog.i("Unregister BroadcastService")
        if (receiver != null) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was created but never successfully registered.
                AppLog.e(e)
            }
            receiver = null
        }

        // Skip DI-dependent cleanup when Koin isn't available to avoid crashing in teardown.
        if (GlobalContext.getOrNull() == null) {
            return
        }
        try {
            val prefs = get<InCarSettings>()
            if (!prefs.isActivityRequired) {
                ActivityTransitionTracker(context).stop()
            }
        } catch (e: Exception) {
            AppLog.e(e)
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
            try {
                ContextCompat.startForegroundService(context, service)
            } catch (e: Exception) {
                // API 31+: ForegroundServiceStartNotAllowedException when started from the
                // background without an exemption (the triggering broadcasts - headset plug,
                // power, Bluetooth ACL - are not exempt). Nothing actionable; skip rather than crash.
                AppLog.e(e)
            }
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