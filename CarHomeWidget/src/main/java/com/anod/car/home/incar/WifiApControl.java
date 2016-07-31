package com.anod.car.home.incar;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import info.anodsplace.android.log.AppLog;

import java.lang.reflect.Method;

/**
 * This class is use to handle all Hotspot related information.
 * 
 *
 * 
 */
public class WifiApControl {
    private static Method getWifiApState;
    private static Method isWifiApEnabled;
    private static Method setWifiApEnabled;
    private static Method getWifiApConfiguration;
 
    static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
 
    static final int WIFI_AP_STATE_DISABLED = WifiManager.WIFI_STATE_DISABLED;
    static final int WIFI_AP_STATE_DISABLING = WifiManager.WIFI_STATE_DISABLING;
    static final int WIFI_AP_STATE_ENABLED = WifiManager.WIFI_STATE_ENABLED;
    static final int WIFI_AP_STATE_ENABLING = WifiManager.WIFI_STATE_ENABLING;
    static final int WIFI_AP_STATE_FAILED = WifiManager.WIFI_STATE_UNKNOWN;
 
    static final String EXTRA_PREVIOUS_WIFI_AP_STATE = WifiManager.EXTRA_PREVIOUS_WIFI_STATE;
    static final String EXTRA_WIFI_AP_STATE = WifiManager.EXTRA_WIFI_STATE;
 
    static {
        // lookup methods and fields not defined publicly in the SDK.
        Class<?> cls = WifiManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.equals("getWifiApState")) {
                getWifiApState = method;
            } else if (methodName.equals("isWifiApEnabled")) {
                isWifiApEnabled = method;
            } else if (methodName.equals("setWifiApEnabled")) {
                setWifiApEnabled = method;
            } else if (methodName.equals("getWifiApConfiguration")) {
                getWifiApConfiguration = method;
            }
        }
    }
 
    public static boolean isApSupported() {
        return (getWifiApState != null && isWifiApEnabled != null
                && setWifiApEnabled != null && getWifiApConfiguration != null);
    }
 
    private WifiManager mgr;
 
    private WifiApControl(WifiManager mgr) {
        this.mgr = mgr;
    }
 
    public static WifiApControl getApControl(WifiManager mgr) {
        if (!isApSupported())
            return null;
        return new WifiApControl(mgr);
    }
 
    public boolean isWifiApEnabled() {
        try {
            return (Boolean) isWifiApEnabled.invoke(mgr);
        } catch (Exception e) {
            AppLog.e(e); // shouldn't happen
            return false;
        }
    }
 
    public int getWifiApState() {
        try {
            return (Integer) getWifiApState.invoke(mgr);
        } catch (Exception e) {
            AppLog.e(e); // shouldn't happen
            return -1;
        }
    }
 
    public WifiConfiguration getWifiApConfiguration() {
        try {
            return (WifiConfiguration) getWifiApConfiguration.invoke(mgr);
        } catch (Exception e) {
            AppLog.e(e); // shouldn't happen
            return null;
        }
    }
 
    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        try {
            return (Boolean) setWifiApEnabled.invoke(mgr, config, enabled);
        } catch (Exception e) {
            AppLog.e(e); // shouldn't happen
            return false;
        }
    }
}