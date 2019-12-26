package com.anod.car.home.incar

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import com.anod.car.home.BuildConfig
import info.anodsplace.framework.AppLog
import java.lang.reflect.Method

/**
 * This class is use to handle all Hotspot related information.
 */
class WifiApControl internal constructor(private val wifiManager: WifiManager) {


    private val wifiApConfiguration: WifiConfiguration?
        get() {
            return try {
                methodGetWifiApConfiguration?.invoke(wifiManager) as WifiConfiguration
            } catch (e: Exception) {
                AppLog.e(e)
                null
            }

        }

    var isEnabled: Boolean
        get() {
            return try {
                methodIsWifiApEnabled?.invoke(wifiManager) as Boolean
            } catch (e: Exception) {
                AppLog.e(e)
                false
            }
        }
        set(value) {
            try {
                wifiApConfiguration?.let {
                    methodSetWifiApEnabled?.invoke(wifiManager, it, value) as Boolean
                }
            } catch (e: Exception) {
                AppLog.e(e)
             }
        }


    companion object {
        private var methodIsWifiApEnabled: Method? = null
        private var methodSetWifiApEnabled: Method? = null
        private var methodGetWifiApConfiguration: Method? = null

        init {
            // lookup methods and fields not defined publicly in the SDK.
            val cls = WifiManager::class.java
            for (method in cls.declaredMethods) {
                when (method.name) {
                    "isWifiApEnabled" -> methodIsWifiApEnabled = method
                    "setWifiApEnabled" -> methodSetWifiApEnabled = method
                    "getWifiApConfiguration" -> methodGetWifiApConfiguration = method
                }
            }


        }

        val isSupported: Boolean
            get() = BuildConfig.VERSION_CODE < Build.VERSION_CODES.O &&
                (methodIsWifiApEnabled != null && methodSetWifiApEnabled != null
                    && methodGetWifiApConfiguration != null)
    }
}