package com.anod.car.home.incar

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * @author alex
 * @date 9/19/13
 */
object SamsungDrivingMode {

    const val DRIVING_MODE_ON = "driving_mode_on"
    const val DEVICE_SAMSUNG = "samsung"
    private val IS_SAMSUNG = Build.MANUFACTURER == DEVICE_SAMSUNG

    fun hasMode(): Boolean {
        return IS_SAMSUNG
    }

    fun enabled(context: Context): Boolean {
        val v = Settings.System.getInt(context.contentResolver, DRIVING_MODE_ON, 0)
        return v == 1
    }

    fun enable(context: Context) {
        Settings.System.putInt(context.contentResolver, DRIVING_MODE_ON, 1)
    }

    fun disable(context: Context) {
        Settings.System.putInt(context.contentResolver, DRIVING_MODE_ON, 0)
    }

}
