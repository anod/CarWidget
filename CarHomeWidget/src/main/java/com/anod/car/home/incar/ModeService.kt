package com.anod.car.home.incar

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.app.App
import com.anod.car.home.notifications.InCarModeNotification
import com.anod.car.home.notifications.TrialExpiredNotification
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.Version

class ModeService : Service() {
    private var phoneListener: ModePhoneStateListener? = null
    private val modeHandler: ModeHandler by lazy { App.provide(this).handler }
    private var forceState: Boolean = false

    override fun onDestroy() {
        stopForeground(true)

        val prefs = InCarStorage.load(this)
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

        val redelivered = flags and Service.START_FLAG_REDELIVERY == Service.START_FLAG_REDELIVERY
        AppLog.d("ModeService onStartCommand, sInCarMode = " + sInCarMode + ", redelivered = "
                + redelivered)

        if (intent == null) {
            AppLog.e("ModeService started without intent")
            stopSelf()
            return Service.START_NOT_STICKY
        }

        val mode = intent.getIntExtra(EXTRA_MODE, -1)
        if (mode == -1) {
            AppLog.e("ModeService, start mode is not correct")
            stopSelf()
            return Service.START_NOT_STICKY
        }
        if (mode == MODE_SWITCH_OFF) {
            stopSelf()
            return Service.START_NOT_STICKY
        }
        // mode == MODE_SWITCH_ON
        forceState = intent.getBooleanExtra(EXTRA_FORCE_STATE, false)

        val version = Version(this)
        if (version.isFreeAndTrialExpired) {
            TrialExpiredNotification.show(this)
            stopSelf()
            return Service.START_NOT_STICKY
        }

        val prefs = InCarStorage.load(this)
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

        startForeground(InCarModeNotification.id, InCarModeNotification.create(version, this))

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return Service.START_REDELIVER_INTENT
    }

    private fun initPhoneListener(prefs: InCarInterface) {
        if (prefs.isAutoSpeaker || prefs.autoAnswer != InCarInterface.AUTOANSWER_DISABLED) {
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
        AppLog.d("Attach phone listener")
        val provider = App.provide(this)
        phoneListener = provider.modePhoneStateListener
        provider.telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun detachPhoneListener() {
        AppLog.d("Detach phone listener")
        val tm = App.provide(this).telephonyManager
        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE)
        phoneListener!!.cancelActions()
        phoneListener = null
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {

        private const val wakelockTag = "carhomewidget:wakelock"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_FORCE_STATE = "extra_force_state"

        const val MODE_SWITCH_OFF = 1
        const val MODE_SWITCH_ON = 0

        var sInCarMode: Boolean = false

        @Volatile
        private var sLockStatic: PowerManager.WakeLock? = null

        @Synchronized
        private fun getLock(context: Context): PowerManager.WakeLock? {
            if (sLockStatic == null) {
                val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                sLockStatic = mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, wakelockTag)
            }

            return sLockStatic
        }

        fun isWakeLockHeld(context: Context): Boolean {
            val lock = ModeService.getLock(context.applicationContext)
            return lock!!.isHeld
        }

        fun acquireWakeLock(context: Context) {
            val lock = ModeService.getLock(context.applicationContext)
            if (!lock!!.isHeld) {
                AppLog.d("WakeLock is not held")
                lock.acquire()
            }
            AppLog.d("WakeLock acquired")
        }

        fun releaseWakeLock(context: Context) {
            val lock = ModeService.getLock(context.applicationContext)

            if (lock!!.isHeld) {
                AppLog.d("WakeLock is held")
                lock.release()
            }
            AppLog.d("WakeLock released")
        }

        fun createStartIntent(context: Context, mode: Int): Intent {
            val service = Intent(context, ModeService::class.java)
            service.putExtra(ModeService.EXTRA_MODE, mode)
            return service
        }
    }
}
