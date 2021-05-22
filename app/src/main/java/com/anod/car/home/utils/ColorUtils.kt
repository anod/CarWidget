package com.anod.car.home.utils

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

import info.anodsplace.applog.AppLog

fun Int.toColorHex(withAlpha: Boolean): String {
    var hexStr = String.format("%08X", this)
    if (!withAlpha) {
        hexStr = hexStr.substring(2)
    }
    return hexStr
}

fun Int.withAlpha(alpha: Int): Int {
    return Color.argb(alpha, this.red, this.green, this.blue)
}

val Int.opaque: Int
    get() = Color.argb(255, this.red, this.green, this.blue)

object ColorUtils {

    fun fromHex(hexStr: String, addAlpha: Boolean, defColor: Int): Int {
        var intValue: Int
        try {
            intValue = Color.parseColor("#$hexStr")
        } catch (e: IllegalArgumentException) {
            AppLog.e(e)
            return defColor
        }

        if (!addAlpha) {
            intValue = (intValue and 0x00FFFFFF) + -0x1000000
        }
        return intValue
    }

    /**
     * Create an array of int with colors
     */
    fun colorChoice(context: Context, resId: Int): IntArray {
        return context.resources.getStringArray(resId).map { Color.parseColor(it) }.toIntArray()
    }
}