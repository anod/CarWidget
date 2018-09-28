package com.anod.car.home.incar

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import info.anodsplace.framework.AppLog
import java.lang.reflect.Method

/**
 * This class is use to handle all Hotspot related information.
 *
 *
 *
 */
class WifiApControl private constructor(private val mgr: WifiManager) {

    val wifiApState: Int
        get() {
            try {
                return getWifiApState!!.invoke(mgr) as Int
            } catch (e: Exception) {
                AppLog.e(e)
                return -1
            }

        }

    val wifiApConfiguration: WifiConfiguration?
        get() {
            try {
                return getWifiApConfiguration!!.invoke(mgr) as WifiConfiguration
            } catch (e: Exception) {
                AppLog.e(e)
                return null
            }

        }

    fun isWifiApEnabled(): Boolean {
        try {
            return isWifiApEnabled!!.invoke(mgr) as Boolean
        } catch (e: Exception) {
            AppLog.e(e) // shouldn't happen
            return false
        }
    }

    fun setWifiApEnabled(config: WifiConfiguration, enabled: Boolean): Boolean {
        try {
            return setWifiApEnabled!!.invoke(mgr, config, enabled) as Boolean
        } catch (e: Exception) {
            AppLog.e(e) // shouldn't happen
            return false
        }

    }

    companion object {
        private var getWifiApState: Method? = null
        private var isWifiApEnabled: Method? = null
        private var setWifiApEnabled: Method? = null
        private var getWifiApConfiguration: Method? = null

        internal const val WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED"

        internal const val WIFI_AP_STATE_DISABLED = WifiManager.WIFI_STATE_DISABLED
        internal const val WIFI_AP_STATE_DISABLING = WifiManager.WIFI_STATE_DISABLING
        internal const val WIFI_AP_STATE_ENABLED = WifiManager.WIFI_STATE_ENABLED
        internal const val WIFI_AP_STATE_ENABLING = WifiManager.WIFI_STATE_ENABLING
        internal const val WIFI_AP_STATE_FAILED = WifiManager.WIFI_STATE_UNKNOWN

        internal const val EXTRA_PREVIOUS_WIFI_AP_STATE = WifiManager.EXTRA_PREVIOUS_WIFI_STATE
        internal const val EXTRA_WIFI_AP_STATE = WifiManager.EXTRA_WIFI_STATE

        init {
            // lookup methods and fields not defined publicly in the SDK.
            val cls = WifiManager::class.java
            for (method in cls.declaredMethods) {
                val methodName = method.name
                if (methodName == "getWifiApState") {
                    getWifiApState = method
                } else if (methodName == "isWifiApEnabled") {
                    isWifiApEnabled = method
                } else if (methodName == "setWifiApEnabled") {
                    setWifiApEnabled = method
                } else if (methodName == "getWifiApConfiguration") {
                    getWifiApConfiguration = method
                }
            }
        }

        private val isApSupported: Boolean
            get() = (getWifiApState != null && isWifiApEnabled != null
                    && setWifiApEnabled != null && getWifiApConfiguration != null)

        fun getApControl(mgr: WifiManager): WifiApControl? {
            return if (!isApSupported) null else WifiApControl(mgr)
        }
    }
}