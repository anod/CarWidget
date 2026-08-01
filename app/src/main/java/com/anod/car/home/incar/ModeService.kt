package com.anod.car.home.incar

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.notifications.Channels
import com.anod.car.home.notifications.InCarModeNotificationFactory
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext


class ModeService : Service(), KoinComponent {
    private var phoneListener: ModePhoneStateListener? = null
    private val modeHandler: ModeHandler by inject()
    private var forceState: Boolean = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val notificationFactory: InCarModeNotificationFactory by lazy {
        InCarModeNotificationFactory(
            context = this,
            database = get(),
            iconLoader = get(),
            shortcutResources = get()
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        sInCarMode = false

        // If Koin never started for this (restarted) process, skip all DI-dependent teardown
        // instead of crashing again with "KoinApplication has not been started".
        if (GlobalContext.getOrNull() == null) {
            AppLog.e("Koin is not started, skipping ModeService teardown")
            super.onDestroy()
            return
        }

        try {
            val prefs = get<InCarSettings>()
            if (forceState) {
                ModeDetector.forceState(prefs, false)
            }
            ModeDetector.switchOff(prefs, modeHandler)
            if (phoneListener != null) {
                detachPhoneListener()
            }
            requestWidgetsUpdate()
        } catch (e: Exception) {
            AppLog.e(e)
        }

        super.onDestroy()
    }

    private fun requestWidgetsUpdate() {
        Provider.requestUpdate(this, intArrayOf(), appWidgetManager = get())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val redelivered = flags and START_FLAG_REDELIVERY == START_FLAG_REDELIVERY
        AppLog.i("Start InCar Mode service, sInCarMode = " + sInCarMode + ", redelivered = "
                + redelivered)

        // Enter the foreground immediately with a dependency-free notification so we never miss
        // the foreground-service start deadline, even if the OS restarted us before the app
        // (Koin/DB) finished initializing.
        startForeground(InCarModeNotificationFactory.id, createSafeNotification())

        // If the OS restarted this service into a process where Koin isn't ready yet, stop
        // cleanly instead of crashing on the first get<>() ("KoinApplication has not been started").
        if (GlobalContext.getOrNull() == null) {
            AppLog.e("Koin is not started, stopping ModeService")
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent == null) {
            AppLog.e("ModeService started without intent")
            stopSelf()
            return START_NOT_STICKY
        }

        val mode = intent.getIntExtra(EXTRA_MODE, -1)
        if (mode == -1) {
            AppLog.e("ModeService, start mode is not correct")
            stopSelf()
            return START_NOT_STICKY
        }
        if (mode == MODE_SWITCH_OFF) {
            stopSelf()
            return START_NOT_STICKY
        }
        // mode == MODE_SWITCH_ON
        forceState = intent.getBooleanExtra(EXTRA_FORCE_STATE, false)

        val prefs = get<InCarSettings>()
        sInCarMode = true
        if (forceState) {
            ModeDetector.forceState(prefs, true)
        }

        ModeDetector.switchOn(prefs, modeHandler)
        initPhoneListener(prefs)
        requestWidgetsUpdate()

        updateNotification()

        // START_NOT_STICKY on purpose: letting the OS auto-restart this foreground service from the
        // background (START_STICKY/START_REDELIVER_INTENT) is the top Play vitals crash on API 31+
        // (ForegroundServiceDidNotStartInTimeException) because a slow cold start misses the
        // startForeground() deadline. In-car mode is re-triggered on demand by ModeBroadcastReceiver
        // and settings changes, so it does not depend on the OS restarting this service.
        return START_NOT_STICKY
    }

    /**
     * Minimal, dependency-free notification used to satisfy the foreground-service start deadline
     * before (or without) Koin. Building the rich notification requires Koin-injected collaborators
     * and database access, which may not be available when the OS restarts the service.
     */
    private fun createSafeNotification(): Notification {
        return NotificationCompat.Builder(this, Channels.inCarMode)
            .setSmallIcon(info.anodsplace.carwidget.skin.R.drawable.ic_stat_incar)
            .setContentTitle(getString(info.anodsplace.carwidget.content.R.string.incar_mode_enabled))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun updateNotification() {
        serviceScope.launch {
            try {
                val notification = withContext(Dispatchers.IO) { notificationFactory.create() }
                if (sInCarMode) {
                    getSystemService(NotificationManager::class.java)
                        ?.notify(InCarModeNotificationFactory.id, notification)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }
    }

    private fun initPhoneListener(prefs: info.anodsplace.carwidget.content.preferences.InCarInterface) {
        if (prefs.isAutoSpeaker || prefs.autoAnswer != info.anodsplace.carwidget.content.preferences.InCarInterface.AUTOANSWER_DISABLED) {
            if (phoneListener == null) {
                attachPhoneListener()
            }
            phoneListener!!.setActions(prefs.isAutoSpeaker, prefs.autoAnswer)
        } else {
            if (phoneListener != null) {
                detachPhoneListener()
            }
        }
    }

    private fun attachPhoneListener() {
        AppLog.i("Attach phone listener")
        phoneListener = get()
        if (AppPermissions.isGranted(get(), AppPermission.PhoneStateRead)) {
            get<TelephonyManager>().listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun detachPhoneListener() {
        AppLog.i("Detach phone listener")
        try {
            if (AppPermissions.isGranted(get(), AppPermission.PhoneStateRead)) {
                get<TelephonyManager>().listen(phoneListener, PhoneStateListener.LISTEN_NONE)
                phoneListener?.cancelActions()
            }
        } catch (e: Exception) {
            AppLog.e(e)
        }
        phoneListener = null
    }

    private val binder = NotificationServiceBinder()

    inner class NotificationServiceBinder : Binder() {
        val service: ModeService
            get() = this@ModeService
    }

    override fun onBind(arg0: Intent): IBinder {
        return binder
    }

    companion object {

        private const val wakelockTag = "com.anod.car.home.incar/.ModeService:wakelock"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_FORCE_STATE = "extra_force_state"

        const val MODE_SWITCH_OFF = 1
        const val MODE_SWITCH_ON = 0

        @Volatile
        var sInCarMode: Boolean = false

        @Volatile
        private var sLockStatic: PowerManager.WakeLock? = null

        @Synchronized
        fun getLock(context: Context): PowerManager.WakeLock? {
            if (sLockStatic == null) {
                val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                sLockStatic = mgr.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        wakelockTag
                )
            }
            return sLockStatic
        }

        fun acquireWakeLock(context: Context) {
            val lock = getLock(context.applicationContext)!!
            if (!lock.isHeld) {
                AppLog.i("WakeLock is not held")
                lock.acquire()
                lock.setReferenceCounted(false)
            }
            AppLog.i("WakeLock acquired")
        }

        fun releaseWakeLock(context: Context) {
            val lock = getLock(context.applicationContext)!!

            if (lock.isHeld) {
                AppLog.i("WakeLock is held")
                lock.release()
            }
            sLockStatic = null
            AppLog.i("WakeLock released")
        }

        fun createStartIntent(context: Context, mode: Int) =
                Intent(context, ModeService::class.java).apply {
                    putExtra(EXTRA_MODE, mode)
                }
    }
}