package com.anod.car.home.incar

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import com.anod.car.home.app.App
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.notifications.InCarModeNotification
import com.anod.car.home.notifications.TrialExpiredNotification
import info.anodsplace.carwidget.content.Version
import info.anodsplace.applog.AppLog

class ModeService : Service() {
    private var phoneListener: ModePhoneStateListener? = null
    private val modeHandler: ModeHandler by lazy { App.provide(this).handler }
    private var forceState: Boolean = false

    override fun onDestroy() {
        stopForeground(true)

        val prefs = info.anodsplace.carwidget.content.preferences.InCarStorage.load(this)
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
        Provider.requestUpdate(this, intArrayOf())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val redelivered = flags and START_FLAG_REDELIVERY == START_FLAG_REDELIVERY
        AppLog.i("Start InCar Mode service, sInCarMode = " + sInCarMode + ", redelivered = "
                + redelivered)

        val version = Version(this)
        startForeground(InCarModeNotification.id, InCarModeNotification.create(version, this))

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

        if (version.isFreeAndTrialExpired) {
            TrialExpiredNotification.show(this)
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = info.anodsplace.carwidget.content.preferences.InCarStorage.load(this)
        sInCarMode = true
        if (forceState) {
            ModeDetector.forceState(prefs, true)
        }

        ModeDetector.switchOn(prefs, modeHandler)
        initPhoneListener(prefs)
        requestWidgetsUpdate()

        if (version.isFree) {
            version.increaseTrialCounter()
        }

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
        val provider = App.provide(this)
        phoneListener = provider.modePhoneStateListener
        provider.telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun detachPhoneListener() {
        AppLog.i("Detach phone listener")
        val tm = App.provide(this).telephonyManager
        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE)
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
