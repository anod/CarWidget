package com.anod.car.home.prefs.model

import android.content.ComponentName
import androidx.collection.ArrayMap
import com.anod.car.home.incar.ScreenOnAlert

/**
 * @author algavris
 * @date 08/04/2016.
 */
interface InCarInterface {
    var isInCarEnabled: Boolean
    var isAutoSpeaker: Boolean
    var btDevices: ArrayMap<String, String>
    var isPowerRequired: Boolean
    var isHeadsetRequired: Boolean
    val isBluetoothRequired: Boolean
    var isDisableBluetoothOnPower: Boolean
    var isEnableBluetoothOnPower: Boolean
    var isDisableScreenTimeout: Boolean
    var isAdjustVolumeLevel: Boolean
    var isActivityRequired: Boolean
    var mediaVolumeLevel: Int
    var callVolumeLevel: Int
    var isEnableBluetooth: Boolean
    var brightness: String
    var isCarDockRequired: Boolean
    var disableWifi: String
    var isActivateCarMode: Boolean
    var autoAnswer: String
    var autorunApp: ComponentName?
    var isSamsungDrivingMode: Boolean
    var isDisableScreenTimeoutCharging: Boolean
    var screenOrientation: Int
    var isHotspotOn: Boolean
    var screenOnAlert: ScreenOnAlert.Settings

    companion object {
        const val BRIGHTNESS_DISABLED = "disabled"
        const val BRIGHTNESS_AUTO = "auto"
        const val BRIGHTNESS_DAY = "day"
        const val BRIGHTNESS_NIGHT = "night"
        const val AUTOANSWER_DISABLED = "disabled"
        const val AUTOANSWER_IMMEDIATLY = "immediately"
        const val AUTOANSWER_DELAY_5 = "delay-5"
        const val WIFI_NOACTION = "no_action"
        const val WIFI_TURNOFF = "turn_off_wifi"
        const val WIFI_DISABLE = "disable_wifi"
        const val DEFAULT_VOLUME_LEVEL = 80
    }
}
