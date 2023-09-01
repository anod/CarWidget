package info.anodsplace.carwidget.content.preferences

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.collection.ArrayMap
import info.anodsplace.ktx.equalsHash
import info.anodsplace.ktx.hashCodeOf

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
    var isDisableScreenTimeout: Boolean
    var isAdjustVolumeLevel: Boolean
    var isActivityRequired: Boolean
    var mediaVolumeLevel: Int
    var callVolumeLevel: Int
    var brightness: String
    var isCarDockRequired: Boolean
    var isActivateCarMode: Boolean
    var autoAnswer: String
    var autorunApp: ComponentName?
    var isDisableScreenTimeoutCharging: Boolean
    var screenOrientation: Int
    var screenOnAlert: ScreenOnAlertSettings

    fun queueChange(key: String, value: Any?)
    fun applyChange(key: String, value: Any?)
    fun applyPending()

    companion object {
        const val BRIGHTNESS_DISABLED = "disabled"
        const val BRIGHTNESS_AUTO = "auto"
        const val BRIGHTNESS_DAY = "day"
        const val BRIGHTNESS_NIGHT = "night"
        const val AUTOANSWER_DISABLED = "disabled"
        const val AUTOANSWER_IMMEDIATLY = "immediately"
        const val AUTOANSWER_DELAY_5 = "delay-5"
        const val DEFAULT_VOLUME_LEVEL = 80

        const val SCREEN_ORIENTATION_DISABLED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        const val SCREEN_ORIENTATION_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        const val SCREEN_ORIENTATION_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        const val SCREEN_ORIENTATION_LANDSCAPE_REVERSE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
    }

    data class ScreenOnAlertSettings(val enabled: Boolean, val loc: Array<Int>) {
        constructor(enabled: Boolean, settings: ScreenOnAlertSettings) : this(enabled, settings.loc)

        fun withLocation(lastX: Int, lastY: Int) = ScreenOnAlertSettings(enabled, arrayOf(lastX, lastY))

        override fun equals(other: Any?): Boolean = equalsHash(this, other)

        override fun hashCode(): Int = hashCodeOf(enabled, loc)

        companion object {
            const val defaultX = 25
            const val defaultY = 260
        }
    }

    data class NoOp(
        override var isInCarEnabled: Boolean = false,
        override var isAutoSpeaker: Boolean = false,
        override var btDevices: ArrayMap<String, String> = ArrayMap(),
        override var isPowerRequired: Boolean = false,
        override var isHeadsetRequired: Boolean = false,
        override val isBluetoothRequired: Boolean = false,
        override var isDisableScreenTimeout: Boolean = false,
        override var isAdjustVolumeLevel: Boolean = false,
        override var isActivityRequired: Boolean = false,
        override var mediaVolumeLevel: Int = 0,
        override var callVolumeLevel: Int = 0,
        override var brightness: String = BRIGHTNESS_DISABLED,
        override var isCarDockRequired: Boolean = false,
        override var isActivateCarMode: Boolean = false,
        override var autoAnswer: String = AUTOANSWER_DISABLED,
        override var autorunApp: ComponentName? = null,
        override var isDisableScreenTimeoutCharging: Boolean = false,
        override var screenOrientation: Int = SCREEN_ORIENTATION_DISABLED,
        override var screenOnAlert: ScreenOnAlertSettings = ScreenOnAlertSettings(false, arrayOf())
    ) : InCarInterface {

        constructor(other: InCarInterface) : this(
               isInCarEnabled = other.isInCarEnabled,
               isAutoSpeaker = other.isAutoSpeaker,
               btDevices = other.btDevices,
               isPowerRequired = other.isPowerRequired,
               isHeadsetRequired = other.isHeadsetRequired,
               isBluetoothRequired = other.isBluetoothRequired,
               isDisableScreenTimeout = other.isDisableScreenTimeout,
               isAdjustVolumeLevel = other.isAdjustVolumeLevel,
               isActivityRequired = other.isActivityRequired,
               mediaVolumeLevel = other.mediaVolumeLevel,
               callVolumeLevel = other.callVolumeLevel,
               brightness = other.brightness,
               isCarDockRequired = other.isCarDockRequired,
               isActivateCarMode = other.isActivateCarMode,
               autoAnswer = other.autoAnswer,
               autorunApp = other.autorunApp,
               isDisableScreenTimeoutCharging = other.isDisableScreenTimeoutCharging,
               screenOrientation = other.screenOrientation,
               screenOnAlert = other.screenOnAlert,
        )


        override fun queueChange(key: String, value: Any?) { throw IllegalAccessError("Not implemented") }
        override fun applyChange(key: String, value: Any?) { throw IllegalAccessError("Not implemented")  }
        override fun applyPending() { throw IllegalAccessError("Not implemented")  }
    }
}