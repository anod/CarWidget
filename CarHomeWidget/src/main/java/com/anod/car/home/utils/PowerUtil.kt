package com.anod.car.home.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * @author alex
 * @date 12/24/13
 */
object PowerUtil {
    fun isConnected(context: Context): Boolean {
        val intent = context
                .registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB
    }
}