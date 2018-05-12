package com.anod.car.home.app

import info.anodsplace.framework.AppLog

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * @author alex
 * @date 12/19/13
 */
abstract class StoppableService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not implemented")
    }

    /**
     * stop or start an mActivator based on the mActivator type and if an
     * mActivator is currently running
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.hasExtra(STOP_INTENT_KEY)) {
                AppLog.d("Stop service intent")
                onBeforeStop(intent)
                stopSelf()
            } else {
                AppLog.d("Start service intent")
                onAfterStart(intent)
            }
        }

        // restart in case the Service gets canceled
        return Service.START_REDELIVER_INTENT
    }

    protected abstract fun onBeforeStop(intent: Intent)

    protected abstract fun onAfterStart(intent: Intent)

    companion object {

        /**
         * send this when external code wants the Service to stop
         */
        const val STOP_INTENT_KEY = "STOP_INTENT_KEY"


        fun fillStopIntent(intent: Intent) {
            intent.putExtra(STOP_INTENT_KEY, true)
        }
    }

}
