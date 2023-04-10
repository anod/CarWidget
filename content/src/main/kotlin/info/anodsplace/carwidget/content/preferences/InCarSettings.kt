package info.anodsplace.carwidget.content.preferences

import android.content.ComponentName
import android.content.Context
import android.text.TextUtils
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.collection.ArrayMap
import androidx.collection.SimpleArrayMap
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.extentions.toComponentName
import info.anodsplace.carwidget.content.preferences.InCarInterface.Companion.SCREEN_ORIENTATION_DISABLED
import java.io.IOException

/**
 * @author algavris
 * @date 08/04/2016.
 */
class InCarSettings(context: Context, appScope: AppCoroutineScope)
    : ChangeableSharedPreferences(InCarStorage.getSharedPreferences(context), appScope), InCarInterface {

    override var isInCarEnabled: Boolean
        get() = prefs.getBoolean(INCAR_MODE_ENABLED, false)
        set(enabled) = applyChange(INCAR_MODE_ENABLED, enabled)

    override var isAutoSpeaker: Boolean
        get() = prefs.getBoolean(AUTO_SPEAKER, false)
        set(autoSpeaker) = applyChange(AUTO_SPEAKER, autoSpeaker)

    override var btDevices: ArrayMap<String, String>
        get() {
            val addrStr = prefs.getString(BLUETOOTH_DEVICE_ADDRESSES, null) ?: return ArrayMap()
            val addrs = addrStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val devices = ArrayMap<String, String>(addrs.size)
            for (i in addrs.indices) {
                val addr = addrs[i]
                devices[addr] = addr
            }
            return devices
        }
        set(btDevices) {
            if (btDevices.isEmpty) {
                applyChange(BLUETOOTH_DEVICE_ADDRESSES, null)
            } else {
                val addrStr = TextUtils.join(",", btDevices.values)
                applyChange(BLUETOOTH_DEVICE_ADDRESSES, addrStr)
            }
        }

    override var isPowerRequired: Boolean
        get() = prefs.getBoolean(POWER_REQUIRED, false)
        set(powerRequired) = applyChange(POWER_REQUIRED, powerRequired)

    override var isHeadsetRequired: Boolean
        get() = prefs.getBoolean(HEADSET_REQUIRED, false)
        set(headsetRequired) = applyChange(HEADSET_REQUIRED, headsetRequired)


    override val isBluetoothRequired: Boolean
        get() = prefs.getString(BLUETOOTH_DEVICE_ADDRESSES, null) != null

    override var isDisableScreenTimeout: Boolean
        get() = prefs.getBoolean(SCREEN_TIMEOUT, true)
        set(screenTimeout) = applyChange(SCREEN_TIMEOUT, screenTimeout)

    override var isAdjustVolumeLevel: Boolean
        get() = prefs.getBoolean(ADJUST_VOLUME_LEVEL, false)
        set(adjustVolumeLevel) = applyChange(ADJUST_VOLUME_LEVEL, adjustVolumeLevel)

    override var isActivityRequired: Boolean
        get() = prefs.getBoolean(ACTIVITY_RECOGNITION, false)
        set(activityRequired) = applyChange(ACTIVITY_RECOGNITION, activityRequired)

    override var mediaVolumeLevel: Int
        get() = prefs.getInt(MEDIA_VOLUME_LEVEL, InCarInterface.DEFAULT_VOLUME_LEVEL)
        set(mediaVolumeLevel) = applyChange(MEDIA_VOLUME_LEVEL, mediaVolumeLevel)

    override var callVolumeLevel: Int
        get() = prefs.getInt(CALL_VOLUME_LEVEL, InCarInterface.DEFAULT_VOLUME_LEVEL)
        set(level) = applyChange(CALL_VOLUME_LEVEL, level)

    override var brightness: String
        get() = prefs.getString(BRIGHTNESS, InCarInterface.BRIGHTNESS_DISABLED)!!
        set(brightness) = applyChange(BRIGHTNESS, brightness)

    override var isCarDockRequired: Boolean
        get() = prefs.getBoolean(CAR_DOCK_REQUIRED, false)
        set(carDockRequired) = applyChange(CAR_DOCK_REQUIRED, carDockRequired)

    override var isActivateCarMode: Boolean
        get() = prefs.getBoolean(ACTIVATE_CAR_MODE, false)
        set(activate) = applyChange(ACTIVATE_CAR_MODE, activate)

    override var autoAnswer: String
        get() = prefs.getString(AUTO_ANSWER, InCarInterface.AUTOANSWER_DISABLED)!!
        set(autoAnswer) = applyChange(AUTO_ANSWER, autoAnswer)

    override var autorunApp: ComponentName?
        get() = prefs.getString(AUTORUN_APP, null)?.toComponentName()
        set(autorunApp) = applyChange(AUTORUN_APP, autorunApp)

    override var isDisableScreenTimeoutCharging: Boolean
        get() = prefs.getBoolean(SCREEN_TIMEOUT_CHARGING, false)
        set(disableScreenTimeoutCharging) = applyChange(SCREEN_TIMEOUT_CHARGING, disableScreenTimeoutCharging)

    override var screenOrientation: Int
        get() {
            // PreferenceList stores values as String
            val orientation = prefs.getString(SCREEN_ORIENTATION, SCREEN_ORIENTATION_DISABLED.toString())
            return Integer.parseInt(orientation!!)
        }
        set(screenOrientation) = applyChange(SCREEN_ORIENTATION, screenOrientation.toString())

    override var screenOnAlert: InCarInterface.ScreenOnAlertSettings
        get() = InCarInterface.ScreenOnAlertSettings(
                prefs.getBoolean("screen-on-alert-enabled", false),
                arrayOf(
                        prefs.getInt("screen-on-alert-x", InCarInterface.ScreenOnAlertSettings.defaultX),
                        prefs.getInt("screen-on-alert-y", InCarInterface.ScreenOnAlertSettings.defaultY)
                )
        )
        set(value) {
            queueChange("screen-on-alert-enabled", value.enabled)
            queueChange("screen-on-alert-x", value.loc[0])
            queueChange("screen-on-alert-y", value.loc[1])
            applyPending()
        }

    @Throws(IOException::class)
    fun writeJson(writer: JsonWriter) {
        writer.beginObject()
        writer.name(INCAR_MODE_ENABLED).value(isInCarEnabled)

        writer.name(HEADSET_REQUIRED).value(isHeadsetRequired)
        writer.name(POWER_REQUIRED).value(isPowerRequired)
        writer.name(CAR_DOCK_REQUIRED).value(isCarDockRequired)
        writer.name(ACTIVITY_RECOGNITION).value(isActivityRequired)

        val addressStr = prefs.getString(BLUETOOTH_DEVICE_ADDRESSES, null)
        if (addressStr != null) {
            writer.name(BLUETOOTH_DEVICE_ADDRESSES).value(addressStr)
        }
        writer.name(SCREEN_TIMEOUT).value(isDisableScreenTimeout)
        writer.name(SCREEN_TIMEOUT_CHARGING).value(isDisableScreenTimeoutCharging)
        writer.name(BRIGHTNESS).value(brightness)
        writer.name(ADJUST_VOLUME_LEVEL).value(isAdjustVolumeLevel)
        writer.name(MEDIA_VOLUME_LEVEL).value(mediaVolumeLevel.toLong())
        writer.name(CALL_VOLUME_LEVEL).value(callVolumeLevel.toLong())
        writer.name(AUTO_SPEAKER).value(isAutoSpeaker)
        writer.name(AUTO_ANSWER).value(autoAnswer)
        writer.name(ACTIVATE_CAR_MODE).value(isActivateCarMode)
        val autoRunAppString = prefs.getString(AUTORUN_APP, null)
        if (autoRunAppString != null) {
            writer.name(AUTORUN_APP).value(autoRunAppString)
        }
        val screenOrientation = prefs.getString(SCREEN_ORIENTATION, SCREEN_ORIENTATION_DISABLED.toString())
        writer.name(SCREEN_ORIENTATION).value(screenOrientation)
        val screenOnAlert = screenOnAlert
        writer.name("screen-on-alert").also { alert ->
            alert.beginObject()
            alert.name("enabled").value(screenOnAlert.enabled)
            alert.name("loc").also { loc ->
                loc.beginArray()
                loc.value(screenOnAlert.loc[0])
                loc.value(screenOnAlert.loc[1])
                loc.endArray()
            }
            alert.endObject()
        }
        writer.endObject()
    }

    @Throws(IOException::class)
    fun readJson(reader: JsonReader): Int {

        val types = SimpleArrayMap<String, JsonToken>()
        types.put(INCAR_MODE_ENABLED, JsonToken.BOOLEAN)

        types.put(HEADSET_REQUIRED, JsonToken.BOOLEAN)
        types.put(POWER_REQUIRED, JsonToken.BOOLEAN)
        types.put(CAR_DOCK_REQUIRED, JsonToken.BOOLEAN)
        types.put(ACTIVITY_RECOGNITION, JsonToken.BOOLEAN)

        types.put(BLUETOOTH_DEVICE_ADDRESSES, JsonToken.STRING)

        types.put(SCREEN_TIMEOUT, JsonToken.BOOLEAN)
        types.put(SCREEN_TIMEOUT_CHARGING, JsonToken.BOOLEAN)

        types.put(BRIGHTNESS, JsonToken.STRING)
        types.put(ADJUST_VOLUME_LEVEL, JsonToken.BOOLEAN)
        types.put(MEDIA_VOLUME_LEVEL, JsonToken.NUMBER)
        types.put(CALL_VOLUME_LEVEL, JsonToken.NUMBER)
        types.put(AUTO_SPEAKER, JsonToken.BOOLEAN)
        types.put(AUTO_ANSWER, JsonToken.STRING)
        types.put(ACTIVATE_CAR_MODE, JsonToken.BOOLEAN)
        types.put(AUTORUN_APP, JsonToken.STRING)
        types.put(SAMSUNG_DRIVING_MODE, JsonToken.BOOLEAN)
        types.put(SCREEN_ORIENTATION, JsonToken.STRING)

        reader.beginObject()
        val found = JsonReaderHelper.readValues(reader, types, this) { name, r ->
            when (name) {
                "screen-on-alert" -> {
                    val screenOnAlert = readScreenAlert(r)
                    queueChange("screen-on-alert-enabled", screenOnAlert.enabled)
                    queueChange("screen-on-alert-x", screenOnAlert.loc[0])
                    queueChange("screen-on-alert-y", screenOnAlert.loc[1])
                    true
                }
                else -> false
            }
        }

        reader.endObject()
        return found
    }

    private fun readScreenAlert(reader: JsonReader): InCarInterface.ScreenOnAlertSettings {
        reader.beginObject()
        var enabled = false
        val loc = arrayOf(InCarInterface.ScreenOnAlertSettings.defaultX, InCarInterface.ScreenOnAlertSettings.defaultY)
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "enabled" -> {
                    enabled = reader.nextBoolean()
                }
                "loc" -> {
                    reader.beginArray()
                    loc[0] = reader.nextInt()
                    loc[1] = reader.nextInt()
                    reader.endArray()
                }
                else -> {
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return InCarInterface.ScreenOnAlertSettings(enabled, loc)
    }

    companion object {
        const val INCAR_MODE_ENABLED = "incar-mode-enabled"
        const val HEADSET_REQUIRED = "headset-required"
        const val POWER_REQUIRED = "power-required"
        const val BLUETOOTH_DEVICE_ADDRESSES = "bt-device-addresses"
        const val SCREEN_TIMEOUT = "screen-timeout"
        const val SCREEN_TIMEOUT_CHARGING = "screen-timeout-charging"
        const val BRIGHTNESS = "brightness"
        const val ADJUST_VOLUME_LEVEL = "adjust-volume-level"
        const val MEDIA_VOLUME_LEVEL = "volume-level"
        const val AUTO_SPEAKER = "auto_speaker"
        const val AUTO_ANSWER = "auto_answer"
        const val ACTIVATE_CAR_MODE = "activate-car-mode"
        const val AUTORUN_APP = "autorun-app"
        const val CALL_VOLUME_LEVEL = "call-volume-level"
        const val ACTIVITY_RECOGNITION = "activity-recognition"
        const val SAMSUNG_DRIVING_MODE = "sam_driving_mode"
        const val SCREEN_ORIENTATION = "screen-orientation"
        const val CAR_DOCK_REQUIRED = "car-dock"
    }
}