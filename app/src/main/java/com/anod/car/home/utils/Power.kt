package com.anod.car.home.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import info.anodsplace.framework.AppLog

/**
 * @author alex
 * @date 12/24/13
 */
object Power {
    fun isConnected(context: Context): Boolean {
        try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB
        } catch (e: Exception) {
            AppLog.e(e)
        }
        return false
    }
}