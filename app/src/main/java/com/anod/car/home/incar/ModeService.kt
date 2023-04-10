package com.anod.car.home.incar

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.notifications.InCarModeNotificationFactory
import com.anod.car.home.notifications.TrialExpiredNotification
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.preferences.InCarSettings
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class ModeService : Service(), KoinComponent {
    private var phoneListener: ModePhoneStateListener? = null
    private val modeHandler: ModeHandler by inject()
    private var forceState: Boolean = false

    override fun onDestroy() {
        stopForeground(true)

        val prefs = get<InCarSettings>()
        if (forceState) {
            ModeDetector.forceState(prefs, false)
        }
        ModeDetector.switchOff(prefs, modeHandler)
        if (phoneListener != null) {
            detachPhoneListener()
        }

        sInCarMode = false
        requestWidgetsUpdate()

        super.onDestroy()
    }

    private fun requestWidgetsUpdate() {
        Provider.requestUpdate(this, intArrayOf(), appWidgetManager = get())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val redelivered = flags and START_FLAG_REDELIVERY == START_FLAG_REDELIVERY
        AppLog.i("Start InCar Mode service, sInCarMode = " + sInCarMode + ", redelivered = "
                + redelivered)

        val notificationFactory = InCarModeNotificationFactory(
            context = this,
            database = get(),
            iconLoader = get(),
            shortcutResources = get()
        )
        val notification = runBlocking { notificationFactory.create() }
        startForeground(InCarModeNotificationFactory.id, notification)

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

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_REDELIVER_INTENT
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
        get<TelephonyManager>().listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun detachPhoneListener() {
        AppLog.i("Detach phone listener")
        get<TelephonyManager>().listen(phoneListener, PhoneStateListener.LISTEN_NONE)
        phoneListener!!.cancelActions()
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