package com.anod.car.home.prefs.model;

import android.content.ComponentName;
import androidx.collection.ArrayMap;

/**
 * @author algavris
 * @date 08/04/2016.
 */
public interface InCarInterface {
    String BRIGHTNESS_DISABLED = "disabled";
    String BRIGHTNESS_AUTO = "auto";
    String BRIGHTNESS_DAY = "day";
    String BRIGHTNESS_NIGHT = "night";
    String AUTOANSWER_DISABLED = "disabled";
    String AUTOANSWER_IMMEDIATLY = "immediately";
    String AUTOANSWER_DELAY_5 = "delay-5";
    String WIFI_NOACTION = "no_action";
    String WIFI_TURNOFF = "turn_off_wifi";
    String WIFI_DISABLE = "disable_wifi";
    int DEFAULT_VOLUME_LEVEL = 80;

    boolean isInCarEnabled();

    void setInCarEnabled(boolean enabled);

    boolean isAutoSpeaker();

    ArrayMap<String, String> getBtDevices();

    void setBtDevices(ArrayMap<String, String> btDevices);

    boolean isPowerRequired();

    boolean isHeadsetRequired();

    boolean isBluetoothRequired();

    boolean isDisableBluetoothOnPower();

    boolean isEnableBluetoothOnPower();

    boolean isDisableScreenTimeout();

    boolean isAdjustVolumeLevel();

    boolean isActivityRequired();

    int getMediaVolumeLevel();

    int getCallVolumeLevel();

    boolean isEnableBluetooth();

    String getBrightness();

    void setPowerRequired(boolean powerRequired);

    void setHeadsetRequired(boolean headsetRequired);

    void setActivityRequired(boolean activityRequired);

    boolean isCarDockRequired();

    void setCarDockRequired(boolean carDockRequired);

    void setDisableBluetoothOnPower(boolean disableBluetoothOnPower);

    void setEnableBluetoothOnPower(boolean enableBluetoothOnPower);

    void setDisableScreenTimeout(boolean screenTimeout);

    void setAdjustVolumeLevel(boolean adjustVolumeLevel);

    void setMediaVolumeLevel(int mediaVolumeLevel);

    void setCallVolumeLevel(int level);

    void setEnableBluetooth(boolean bluetooth);

    void setBrightness(String brightness);

    void setAutoSpeaker(boolean autoSpeaker);

    String getDisableWifi();

    void setDisableWifi(String disableWifi);

    boolean isActivateCarMode();

    void setActivateCarMode(boolean activate);

    String getAutoAnswer();

    void setAutoAnswer(String autoAnswer);

    ComponentName getAutorunApp();

    void setAutorunApp(ComponentName autorunApp);

    boolean isSamsungDrivingMode();

    void setSamsungDrivingMode(boolean samsungDrivingMode);

    boolean isDisableScreenTimeoutCharging();

    void setDisableScreenTimeoutCharging(boolean disableScreenTimeoutCharging);

    int getScreenOrientation();

    void setScreenOrientation(int screenOrientation);

    boolean isHotspotOn();

    void setHotspotOn(boolean on);
}
