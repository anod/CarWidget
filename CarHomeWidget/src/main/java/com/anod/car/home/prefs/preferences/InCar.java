package com.anod.car.home.prefs.preferences;

import android.content.ComponentName;
import android.support.v4.util.ArrayMap;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class InCar implements Serializable, InCarInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean inCarEnabled;

    private boolean powerRequired;

    private boolean headsetRequired;

    private boolean bluetoothRequired;

    private ArrayMap<String, String> btDevices;

    private boolean disableBluetoothOnPower;

    private boolean enableBluetoothOnPower;

    private boolean disableScreenTimeout;

    private boolean disableScreenTimeoutCharging;

    private boolean adjustVolumeLevel;

    private int mediaVolumeLevel = 80;

    private int callVolumeLevel = 80;

    private boolean enableBluetooth;

    private String autoAnswer = AUTOANSWER_DISABLED;

    private String brightness = BRIGHTNESS_AUTO;

    private String disableWifi = WIFI_NOACTION;

    private boolean autoSpeaker;

    private boolean activateCarMode;

    private boolean samsungDrivingMode;

    transient private ComponentName autorunApp;

    private boolean activityRequired;

    private boolean mCarDockRequired;

    private int screenOrientation = -1;

    private boolean hotspotOn;

    public InCar() {
        super();
    }

    @Override
    public boolean isInCarEnabled() {
        return inCarEnabled;
    }

    @Override
    public void setInCarEnabled(boolean enabled) {
        this.inCarEnabled = enabled;
    }

    @Override
    public boolean isAutoSpeaker() {
        return autoSpeaker;
    }

    /**
     * Map of addresses of bluetooth device on connect with one of them enable
     * in car mode
     */
    @Override
    public ArrayMap<String, String> getBtDevices() {
        return btDevices;
    }

    @Override
    public void setBtDevices(ArrayMap<String, String> btDevices) {
        this.btDevices = btDevices;
        this.bluetoothRequired = (btDevices != null && !btDevices.isEmpty());
    }

    @Override
    public boolean isPowerRequired() {
        return powerRequired;
    }

    @Override
    public boolean isHeadsetRequired() {
        return headsetRequired;
    }

    @Override
    public boolean isBluetoothRequired() {
        return bluetoothRequired;
    }

    @Override
    public boolean isDisableBluetoothOnPower() {
        return disableBluetoothOnPower;
    }

    @Override
    public boolean isEnableBluetoothOnPower() {
        return enableBluetoothOnPower;
    }

    @Override
    public boolean isDisableScreenTimeout() {
        return disableScreenTimeout;
    }

    @Override
    public boolean isAdjustVolumeLevel() {
        return adjustVolumeLevel;
    }

    @Override
    public boolean isActivityRequired() {
        return activityRequired;
    }

    /**
     * Level 0-100 of media volume
     */
    @Override
    public int getMediaVolumeLevel() {
        return mediaVolumeLevel;
    }

    /**
     * Level 0-100 of media volume
     */
    @Override
    public int getCallVolumeLevel() {
        return callVolumeLevel;
    }

    @Override
    public boolean isEnableBluetooth() {
        return enableBluetooth;
    }

    /**
     * Get brightness preset
     *
     * @see this.BRIGHTNESS_DEFAULT
     * @see this.BRIGHTNESS_AUTO
     * @see this.BRIGHTNESS_DAY
     * @see this.BRIGHTNESS_NIGHT
     */
    @Override
    public String getBrightness() {
        return brightness;
    }

    @Override
    public void setPowerRequired(boolean powerRequired) {
        this.powerRequired = powerRequired;
    }

    @Override
    public void setHeadsetRequired(boolean headsetRequired) {
        this.headsetRequired = headsetRequired;
    }

    @Override
    public void setActivityRequired(boolean activityRequired) {
        this.activityRequired = activityRequired;
    }

    @Override
    public boolean isCarDockRequired() {
        return mCarDockRequired;
    }

    @Override
    public void setCarDockRequired(boolean carDockRequired) {
        mCarDockRequired = carDockRequired;
    }

    @Override
    public void setDisableBluetoothOnPower(boolean disableBluetoothOnPower) {
        this.disableBluetoothOnPower = disableBluetoothOnPower;
    }

    @Override
    public void setEnableBluetoothOnPower(boolean enableBluetoothOnPower) {
        this.enableBluetoothOnPower = enableBluetoothOnPower;
    }

    @Override
    public void setDisableScreenTimeout(boolean screenTimeout) {
        this.disableScreenTimeout = screenTimeout;
    }

    @Override
    public void setAdjustVolumeLevel(boolean adjustVolumeLevel) {
        this.adjustVolumeLevel = adjustVolumeLevel;
    }

    @Override
    public void setMediaVolumeLevel(int mediaVolumeLevel) {
        this.mediaVolumeLevel = mediaVolumeLevel;
    }

    @Override
    public void setCallVolumeLevel(int level) {
        callVolumeLevel = level;
    }

    @Override
    public void setEnableBluetooth(boolean bluetooth) {
        this.enableBluetooth = bluetooth;
    }

    @Override
    public void setBrightness(String brightness) {
        this.brightness = brightness;
    }

    @Override
    public void setAutoSpeaker(boolean autoSpeaker) {
        this.autoSpeaker = autoSpeaker;
    }

    /**
     * Get wifi action preset
     *
     * @see this.WIFI_NOACTION
     * @see this.WIFI_TURNOFF
     * @see this.WIFI_DISABLE
     */
    @Override
    public String getDisableWifi() {
        return disableWifi;
    }

    @Override
    public void setDisableWifi(String disableWifi) {
        this.disableWifi = disableWifi;
    }

    /**
     * Activate or not built-in android Car Mode
     */
    @Override
    public boolean isActivateCarMode() {
        return this.activateCarMode;
    }

    @Override
    public void setActivateCarMode(boolean activate) {
        this.activateCarMode = activate;
    }

    /**
     * Get auto answer mode
     */
    @Override
    public String getAutoAnswer() {
        return autoAnswer;
    }

    @Override
    public void setAutoAnswer(String autoAnswer) {
        this.autoAnswer = autoAnswer;
    }

    /**
     * @return the autorunApp
     */
    @Override
    public ComponentName getAutorunApp() {
        return autorunApp;
    }

    /**
     * @param autorunApp the autorunApp to set
     */
    @Override
    public void setAutorunApp(ComponentName autorunApp) {
        this.autorunApp = autorunApp;
    }

    @Override
    public boolean isSamsungDrivingMode() {
        return samsungDrivingMode;
    }

    @Override
    public void setSamsungDrivingMode(boolean samsungDrivingMode) {
        this.samsungDrivingMode = samsungDrivingMode;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        if (autorunApp != null) {
            out.writeBoolean(true);
            out.writeUTF(autorunApp.getPackageName());
            out.writeUTF(autorunApp.getClassName());
        } else {
            out.writeBoolean(false);
        }

    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();

        boolean hasComponent;
        try {
            hasComponent = in.readBoolean();
        } catch (EOFException e) { // old version compatibility
            return;
        }
        if (hasComponent) {
            String pkg = in.readUTF();
            String cls = in.readUTF();
            autorunApp = new ComponentName(pkg, cls);
        }

    }

    @Override
    public boolean isDisableScreenTimeoutCharging() {
        return disableScreenTimeoutCharging;
    }

    @Override
    public void setDisableScreenTimeoutCharging(boolean disableScreenTimeoutCharging) {
        this.disableScreenTimeoutCharging = disableScreenTimeoutCharging;
    }

    @Override
    public int getScreenOrientation() {
        return screenOrientation;
    }

    @Override
    public void setScreenOrientation(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    @Override
    public boolean isHotspotOn() {
        return hotspotOn;
    }

    @Override
    public void setHotspotOn(boolean on) {
        hotspotOn = on;
    }
}