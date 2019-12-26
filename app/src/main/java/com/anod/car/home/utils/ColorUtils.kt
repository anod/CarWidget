package com.anod.car.home.utils

import android.content.Context
import android.graphics.Color

import info.anodsplace.framework.AppLog

object ColorUtils {

    fun toHex(color: Int, addAlpha: Boolean): String {
        var hexStr = String.format("%08X", color)
        if (!addAlpha) {
            hexStr = hexStr.substring(2)
        }
        return hexStr
    }

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