package com.anod.car.home.prefs.model;

import android.content.ComponentName;
import android.content.SharedPreferences;
import androidx.collection.ArrayMap;
import androidx.collection.SimpleArrayMap;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import com.anod.car.home.incar.SamsungDrivingMode;
import com.anod.car.home.incar.ScreenOrientation;
import com.anod.car.home.utils.Utils;

import java.io.IOException;

/**
 * @author algavris
 * @date 08/04/2016.
 */
public class InCarSettings extends ChangeableSharedPreferences implements InCarInterface {
    public static final String INCAR_MODE_ENABLED = "incar-mode-enabled";
    public static final String POWER_BT_ENABLE = "power-bt-enable";
    public static final String POWER_BT_DISABLE = "power-bt-disable";
    public static final String HEADSET_REQUIRED = "headset-required";
    public static final String POWER_REQUIRED = "power-required";
    public static final String BLUETOOTH_DEVICE_ADDRESSES = "bt-device-addresses";
    public static final String SCREEN_TIMEOUT = "screen-timeout";
    public static final String SCREEN_TIMEOUT_CHARGING = "screen-timeout-charging";
    public static final String BRIGHTNESS = "brightness";
    public static final String BLUETOOTH = "bluetooth";
    public static final String ADJUST_VOLUME_LEVEL = "adjust-volume-level";
    public static final String MEDIA_VOLUME_LEVEL = "volume-level";
    public static final String AUTO_SPEAKER = "auto_speaker";
    public static final String AUTO_ANSWER = "auto_answer";
    public static final String ADJUST_WIFI = "wi-fi";
    public static final String ACTIVATE_CAR_MODE = "activate-car-mode";
    public static final String AUTORUN_APP = "autorun-app";
    public static final String CALL_VOLUME_LEVEL = "call-volume-level";
    public static final String ACTIVITY_RECOGNITION = "activity-recognition";
    public static final String SAMSUNG_DRIVING_MODE = "sam_driving_mode";
    public static final String SCREEN_ORIENTATION = "screen-orientation";
    public static final String CAR_DOCK_REQUIRED = "car-dock";
    public static final String HOTSPOT = "hotspot";

    public InCarSettings(SharedPreferences mPrefs) {
        super(mPrefs);
    }

    @Override
    public boolean isInCarEnabled() {
        return getPrefs().getBoolean(INCAR_MODE_ENABLED, false);
    }

    @Override
    public void setInCarEnabled(boolean enabled) {
        putChange(INCAR_MODE_ENABLED, enabled);
    }

    @Override
    public boolean isAutoSpeaker() {
        return getPrefs().getBoolean(AUTO_SPEAKER, false);
    }

    @Override
    public ArrayMap<String, String> getBtDevices() {
        String addrStr = getPrefs().getString(BLUETOOTH_DEVICE_ADDRESSES, null);
        if (addrStr == null) {
            return null;
        }
        String[] addrs = addrStr.split(",");
        ArrayMap<String, String> devices = new ArrayMap<String, String>(addrs.length);
        for (int i = 0; i < addrs.length; i++) {
            String addr = addrs[i];
            devices.put(addr, addr);
        }
        return devices;
    }

    @Override
    public void setBtDevices(ArrayMap<String, String> btDevices) {
        if (btDevices == null || btDevices.isEmpty()) {
            putChange(BLUETOOTH_DEVICE_ADDRESSES, null);
        } else {
            String addrStr = TextUtils.join(",", btDevices.values());
            putChange(BLUETOOTH_DEVICE_ADDRESSES, addrStr);
        }
    }

    @Override
    public boolean isPowerRequired() {
        return getPrefs().getBoolean(POWER_REQUIRED, false);
    }

    @Override
    public boolean isHeadsetRequired() {
        return getPrefs().getBoolean(HEADSET_REQUIRED, false);
    }

    @Override
    public boolean isBluetoothRequired() {
        return getPrefs().getString(BLUETOOTH_DEVICE_ADDRESSES, null) != null;
    }

    @Override
    public boolean isDisableBluetoothOnPower() {
        return getPrefs().getBoolean(POWER_BT_DISABLE, false);
    }

    @Override
    public boolean isEnableBluetoothOnPower() {
        return getPrefs().getBoolean(POWER_BT_ENABLE, false);
    }

    @Override
    public boolean isDisableScreenTimeout() {
        return getPrefs().getBoolean(SCREEN_TIMEOUT, true);
    }

    @Override
    public boolean isAdjustVolumeLevel() {
        return getPrefs().getBoolean(ADJUST_VOLUME_LEVEL, false);
    }

    @Override
    public boolean isActivityRequired() {
        return getPrefs().getBoolean(ACTIVITY_RECOGNITION, false);
    }

    @Override
    public int getMediaVolumeLevel() {
        return getPrefs().getInt(MEDIA_VOLUME_LEVEL, InCarInterface.DEFAULT_VOLUME_LEVEL);
    }

    @Override
    public int getCallVolumeLevel() {
        return getPrefs().getInt(CALL_VOLUME_LEVEL, InCarInterface.DEFAULT_VOLUME_LEVEL);
    }

    @Override
    public boolean isEnableBluetooth() {
        return getPrefs().getBoolean(BLUETOOTH, false);
    }

    @Override
    public String getBrightness() {
        return getPrefs().getString(BRIGHTNESS, InCarInterface.BRIGHTNESS_DISABLED);
    }

    @Override
    public void setPowerRequired(boolean powerRequired) {
        putChange(POWER_REQUIRED, powerRequired);
    }

    @Override
    public void setHeadsetRequired(boolean headsetRequired) {
        putChange(HEADSET_REQUIRED, headsetRequired);
    }

    @Override
    public void setActivityRequired(boolean activityRequired) {
        putChange(ACTIVITY_RECOGNITION, activityRequired);
    }

    @Override
    public boolean isCarDockRequired() {
        return getPrefs().getBoolean(CAR_DOCK_REQUIRED, false);
    }

    @Override
    public void setCarDockRequired(boolean carDockRequired) {
        putChange(CAR_DOCK_REQUIRED, carDockRequired);
    }

    @Override
    public void setDisableBluetoothOnPower(boolean disableBluetoothOnPower) {
        putChange(POWER_BT_DISABLE, disableBluetoothOnPower);
    }

    @Override
    public void setEnableBluetoothOnPower(boolean enableBluetoothOnPower) {
        putChange(POWER_BT_ENABLE, enableBluetoothOnPower);
    }

    @Override
    public void setDisableScreenTimeout(boolean screenTimeout) {
        putChange(SCREEN_TIMEOUT, screenTimeout);
    }

    @Override
    public void setAdjustVolumeLevel(boolean adjustVolumeLevel) {
        putChange(ADJUST_VOLUME_LEVEL, adjustVolumeLevel);
    }

    @Override
    public void setMediaVolumeLevel(int mediaVolumeLevel) {
        putChange(MEDIA_VOLUME_LEVEL, mediaVolumeLevel);
    }

    @Override
    public void setCallVolumeLevel(int level) {
        putChange(CALL_VOLUME_LEVEL, level);
    }

    @Override
    public void setEnableBluetooth(boolean bluetooth) {
        putChange(BLUETOOTH, bluetooth);
    }

    @Override
    public void setBrightness(String brightness) {
        putChange(BRIGHTNESS, brightness);
    }

    @Override
    public void setAutoSpeaker(boolean autoSpeaker) {
        putChange(AUTO_SPEAKER, autoSpeaker);
    }

    @Override
    public String getDisableWifi() {
        return getPrefs().getString(ADJUST_WIFI, InCarInterface.WIFI_NOACTION);
    }

    @Override
    public void setDisableWifi(String disableWifi) {
        putChange(ADJUST_WIFI, disableWifi);
    }

    @Override
    public boolean isActivateCarMode() {
        return getPrefs().getBoolean(ACTIVATE_CAR_MODE, false);
    }

    @Override
    public void setActivateCarMode(boolean activate) {
        putChange(ACTIVATE_CAR_MODE, activate);
    }

    @Override
    public String getAutoAnswer() {
        return getPrefs().getString(AUTO_ANSWER, InCarInterface.AUTOANSWER_DISABLED);
    }

    @Override
    public void setAutoAnswer(String autoAnswer) {
        putChange(AUTO_ANSWER, autoAnswer);
    }

    @Override
    public ComponentName getAutorunApp() {
        String autorunAppString = getPrefs().getString(AUTORUN_APP, null);

        if (autorunAppString != null) {
            return Utils.INSTANCE.stringToComponent(autorunAppString);
        }
        return null;
    }

    @Override
    public void setAutorunApp(ComponentName autorunApp) {
        putChange(AUTORUN_APP, autorunApp);
    }

    @Override
    public boolean isSamsungDrivingMode() {
        return getPrefs().getBoolean(SAMSUNG_DRIVING_MODE, false);
    }

    @Override
    public void setSamsungDrivingMode(boolean samsungDrivingMode) {
        putChange(SAMSUNG_DRIVING_MODE, samsungDrivingMode);
    }

    @Override
    public boolean isDisableScreenTimeoutCharging() {
        return getPrefs().getBoolean(SCREEN_TIMEOUT_CHARGING, false);
    }

    @Override
    public void setDisableScreenTimeoutCharging(boolean disableScreenTimeoutCharging) {
        putChange(SCREEN_TIMEOUT_CHARGING, disableScreenTimeoutCharging);
    }

    @Override
    public int getScreenOrientation() {
        // PreferenceList stores values as String
        String orientation = getPrefs().getString(SCREEN_ORIENTATION, String.valueOf(ScreenOrientation.Companion.getDISABLED()));
        return Integer.parseInt(orientation);
    }

    @Override
    public void setScreenOrientation(int screenOrientation) {
        putChange(SCREEN_ORIENTATION, String.valueOf(screenOrientation));
    }

    @Override
    public boolean isHotspotOn() {
        return getPrefs().getBoolean(HOTSPOT, false);
    }

    @Override
    public void setHotspotOn(boolean on) {
        putChange(HOTSPOT, on);
    }

    
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(INCAR_MODE_ENABLED).value(isInCarEnabled());

        writer.name(HEADSET_REQUIRED).value(isHeadsetRequired());
        writer.name(POWER_REQUIRED).value(isPowerRequired());
        writer.name(CAR_DOCK_REQUIRED).value(isCarDockRequired());
        writer.name(ACTIVITY_RECOGNITION).value(isActivityRequired());

        writer.name(POWER_BT_ENABLE).value(isEnableBluetoothOnPower());
        writer.name(POWER_BT_DISABLE).value(isDisableBluetoothOnPower());

        String addressStr = getPrefs().getString(BLUETOOTH_DEVICE_ADDRESSES, null);
        if (addressStr != null) {
            writer.name(BLUETOOTH_DEVICE_ADDRESSES).value(addressStr);
        }
        writer.name(SCREEN_TIMEOUT).value(isDisableScreenTimeout());
        writer.name(SCREEN_TIMEOUT_CHARGING).value(isDisableScreenTimeoutCharging());
        writer.name(BRIGHTNESS).value(getBrightness());
        writer.name(BLUETOOTH).value(isEnableBluetooth());
        writer.name(ADJUST_VOLUME_LEVEL).value(isAdjustVolumeLevel());
        writer.name(MEDIA_VOLUME_LEVEL).value(getMediaVolumeLevel());
        writer.name(CALL_VOLUME_LEVEL).value(getCallVolumeLevel());
        writer.name(AUTO_SPEAKER).value(isAutoSpeaker());
        writer.name(AUTO_ANSWER).value(getAutoAnswer());
        writer.name(ADJUST_WIFI).value(getDisableWifi());
        writer.name(ACTIVATE_CAR_MODE).value(isActivateCarMode());
        String autoRunAppString = getPrefs().getString(AUTORUN_APP, null);
        if (autoRunAppString != null) {
            writer.name(AUTORUN_APP).value(autoRunAppString);
        }
        if (SamsungDrivingMode.hasMode()) {
            writer.name(SAMSUNG_DRIVING_MODE).value(isSamsungDrivingMode());
        }
        String screenOrientation = getPrefs().getString(SCREEN_ORIENTATION, String.valueOf(ScreenOrientation.Companion.getDISABLED()));
        writer.name(SCREEN_ORIENTATION).value(screenOrientation);
        writer.name(HOTSPOT).value(isHotspotOn());
        writer.endObject();
    }

    public void readJson(JsonReader reader) throws IOException {
        reader.beginObject();

        SimpleArrayMap<String, JsonToken> types = new SimpleArrayMap<>();
        types.put(INCAR_MODE_ENABLED, JsonToken.BOOLEAN);

        types.put(HEADSET_REQUIRED, JsonToken.BOOLEAN);
        types.put(POWER_REQUIRED, JsonToken.BOOLEAN);
        types.put(CAR_DOCK_REQUIRED, JsonToken.BOOLEAN);
        types.put(ACTIVITY_RECOGNITION, JsonToken.BOOLEAN);

        types.put(POWER_BT_ENABLE, JsonToken.BOOLEAN);
        types.put(POWER_BT_DISABLE, JsonToken.BOOLEAN);

        types.put(BLUETOOTH_DEVICE_ADDRESSES, JsonToken.STRING);

        types.put(SCREEN_TIMEOUT, JsonToken.BOOLEAN);
        types.put(SCREEN_TIMEOUT_CHARGING, JsonToken.BOOLEAN);

        types.put(BRIGHTNESS, JsonToken.STRING);
        types.put(BLUETOOTH, JsonToken.BOOLEAN);
        types.put(ADJUST_VOLUME_LEVEL, JsonToken.BOOLEAN);
        types.put(MEDIA_VOLUME_LEVEL, JsonToken.NUMBER);
        types.put(CALL_VOLUME_LEVEL, JsonToken.NUMBER);
        types.put(AUTO_SPEAKER, JsonToken.BOOLEAN);
        types.put(AUTO_ANSWER, JsonToken.STRING);
        types.put(ADJUST_WIFI, JsonToken.STRING);
        types.put(ACTIVATE_CAR_MODE, JsonToken.BOOLEAN);
        types.put(AUTORUN_APP, JsonToken.STRING);
        types.put(SAMSUNG_DRIVING_MODE, JsonToken.BOOLEAN);
        types.put(SCREEN_ORIENTATION, JsonToken.STRING);
        types.put(HOTSPOT, JsonToken.BOOLEAN);

        JsonReaderHelper.INSTANCE.readValues(reader, types, this);

        reader.endObject();
    }
}
