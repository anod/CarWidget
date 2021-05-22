package info.anodsplace.carwidget.preferences.model

import android.content.ComponentName
import androidx.collection.ArrayMap
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation

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
    var isActivateCarMode: Boolean
    var autoAnswer: String
    var autorunApp: ComponentName?
    var isDisableScreenTimeoutCharging: Boolean
    var screenOrientation: Int
    var screenOnAlert: ScreenOnAlert.Settings

    companion object {
        const val BRIGHTNESS_DISABLED = "disabled"
        const val BRIGHTNESS_AUTO = "auto"
        const val BRIGHTNESS_DAY = "day"
        const val BRIGHTNESS_NIGHT = "night"
        const val AUTOANSWER_DISABLED = "disabled"
        const val AUTOANSWER_IMMEDIATLY = "immediately"
        const val AUTOANSWER_DELAY_5 = "delay-5"
        const val DEFAULT_VOLUME_LEVEL = 80
    }

    class NoOp(
        override var isInCarEnabled: Boolean = false,
        override var isAutoSpeaker: Boolean = false,
        override var btDevices: ArrayMap<String, String> = ArrayMap(),
        override var isPowerRequired: Boolean = false,
        override var isHeadsetRequired: Boolean = false,
        override val isBluetoothRequired: Boolean = false,
        override var isDisableBluetoothOnPower: Boolean = false,
        override var isEnableBluetoothOnPower: Boolean = false,
        override var isDisableScreenTimeout: Boolean = false,
        override var isAdjustVolumeLevel: Boolean = false,
        override var isActivityRequired: Boolean = false,
        override var mediaVolumeLevel: Int = 0,
        override var callVolumeLevel: Int = 0,
        override var isEnableBluetooth: Boolean = false,
        override var brightness: String = BRIGHTNESS_DISABLED,
        override var isCarDockRequired: Boolean = false,
        override var isActivateCarMode: Boolean = false,
        override var autoAnswer: String = AUTOANSWER_DISABLED,
        override var autorunApp: ComponentName? = null,
        override var isDisableScreenTimeoutCharging: Boolean = false,
        override var screenOrientation: Int = ScreenOrientation.DISABLED,
        override var screenOnAlert: ScreenOnAlert.Settings = ScreenOnAlert.Settings(false, arrayOf())
    ) : InCarInterface
}
