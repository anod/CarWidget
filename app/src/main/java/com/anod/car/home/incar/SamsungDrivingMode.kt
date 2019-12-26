package com.anod.car.home.incar

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.anod.car.home.R

/**
 * @author alex
 * @date 9/19/13
 */
object SamsungDrivingMode {

    private const val DRIVING_MODE_ON = "driving_mode_on"
    private const val DEVICE_SAMSUNG = "samsung"

    val hasMode: Boolean
        get() =  Build.MANUFACTURER == DEVICE_SAMSUNG && Build.VERSION.SDK_INT < Build.VERSION_CODES.M

    fun enabled(context: Context): Boolean {
        if (!hasMode) {
            return false
        }
        val v = Settings.System.getInt(context.contentResolver, DRIVING_MODE_ON, 0)
        return v == 1
    }

    fun enable(context: Context) {
        if (hasMode) {
            write(true, context)
        }
    }

    fun disable(context: Context) {
        if (hasMode) {
            write(false, context)
        }
    }

    private fun write(enable: Boolean, context: Context) {
        val value = if (enable) 1 else 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                Settings.System.putInt(context.contentResolver, DRIVING_MODE_ON, value)
            } else {
                Toast.makeText(context, R.string.allow_permissions_samsung_mode, Toast.LENGTH_LONG).show()
            }
        } else {
            Settings.System.putInt(context.contentResolver, DRIVING_MODE_ON, value)
        }
    }
}
