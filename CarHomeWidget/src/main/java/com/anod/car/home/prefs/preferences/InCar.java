package com.anod.car.home.prefs.preferences;

import android.content.ComponentName;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class InCar implements Serializable {
	public static final String BRIGHTNESS_DISABLED = "disabled";
	public static final String BRIGHTNESS_AUTO = "auto";
	public static final String BRIGHTNESS_DAY = "day";
	public static final String BRIGHTNESS_NIGHT = "night";

	public static final String AUTOANSWER_DISABLED = "disabled";
	public static final String AUTOANSWER_IMMEDIATLY = "immediately";
	public static final String AUTOANSWER_DELAY_5 = "delay-5";

	public static final String WIFI_NOACTION = "no_action";
	public static final String WIFI_TURNOFF = "turn_off_wifi";
	public static final String WIFI_DISABLE = "disable_wifi";

	public static final int DEFAULT_VOLUME_LEVEL = 80;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean inCarEnabled;
	private boolean powerRequired;
	private boolean headsetRequired;
	private boolean bluetoothRequired;
	private HashMap<String, String> btDevices;

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

	public InCar() {
		super();
	}

	/**
	 * @return the inCarEnabled
	 */
	public boolean isInCarEnabled() {
		return inCarEnabled;
	}

	/**
	 * @param enabled
	 *            the inCarEnabled to set
	 */
	public void setInCarEnabled(boolean enabled) {
		this.inCarEnabled = enabled;
	}

	/**
	 * Enable auto-speaker when receiving a call
	 * 
	 * @return
	 */
	public boolean isAutoSpeaker() {
		return autoSpeaker;
	}

	/**
	 * Map of addresses of bluetooth device on connect with one of them enable
	 * in car mode
	 * 
	 * @return HashMap<Address,Address>
	 */
	public HashMap<String, String> getBtDevices() {
		return btDevices;
	}

	public void setBtDevices(HashMap<String, String> btDevices) {
		this.btDevices = btDevices;
		this.bluetoothRequired = (btDevices != null && !btDevices.isEmpty());
	}

	public boolean isPowerRequired() {
		return powerRequired;
	}

	public boolean isHeadsetRequired() {
		return headsetRequired;
	}

	public boolean isBluetoothRequired() {
		return bluetoothRequired;
	}

	public boolean isDisableBluetoothOnPower() {
		return disableBluetoothOnPower;
	}

	public boolean isEnableBluetoothOnPower() {
		return enableBluetoothOnPower;
	}

	public boolean isDisableScreenTimeout() {
		return disableScreenTimeout;
	}

	public boolean isAdjustVolumeLevel() {
		return adjustVolumeLevel;
	}

	public boolean isActivityRequired() {
		return activityRequired;
	}
	/**
	 * Level 0-100 of media volume
	 * 
	 * @return
	 */
	public int getMediaVolumeLevel() {
		return mediaVolumeLevel;
	}

	/**
	 * Level 0-100 of media volume
	 *
	 * @return
	 */
	public int getCallVolumeLevel() {
		return callVolumeLevel;
	}

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
	 * 
	 * @return
	 */
	public String getBrightness() {
		return brightness;
	}

	public void setPowerRequired(boolean powerRequired) {
		this.powerRequired = powerRequired;
	}

	public void setHeadsetRequired(boolean headsetRequired) {
		this.headsetRequired = headsetRequired;
	}

	public void setActivityRequired(boolean activityRequired) {
		this.activityRequired = activityRequired;
	}

	public boolean isCarDockRequired() {
		return mCarDockRequired;
	}

	public void setCarDockRequired(boolean carDockRequired) {
		mCarDockRequired = carDockRequired;
	}

	public void setDisableBluetoothOnPower(boolean disableBluetoothOnPower) {
		this.disableBluetoothOnPower = disableBluetoothOnPower;
	}

	public void setEnableBluetoothOnPower(boolean enableBluetoothOnPower) {
		this.enableBluetoothOnPower = enableBluetoothOnPower;
	}

	public void setDisableScreenTimeout(boolean screenTimeout) {
		this.disableScreenTimeout = screenTimeout;
	}

	public void setAdjustVolumeLevel(boolean adjustVolumeLevel) {
		this.adjustVolumeLevel = adjustVolumeLevel;
	}

	public void setMediaVolumeLevel(int mediaVolumeLevel) {
		this.mediaVolumeLevel = mediaVolumeLevel;
	}

	public void setCallVolumeLevel(int level) {
		callVolumeLevel = level;
	}

	public void setEnableBluetooth(boolean bluetooth) {
		this.enableBluetooth = bluetooth;
	}

	public void setBrightness(String brightness) {
		this.brightness = brightness;
	}

	public void setAutoSpeaker(boolean autoSpeaker) {
		this.autoSpeaker = autoSpeaker;
	}

	/**
	 * Get wifi action preset
	 * 
	 * @see this.WIFI_NOACTION
	 * @see this.WIFI_TURNOFF
	 * @see this.WIFI_DISABLE
	 * @return
	 */
	public String getDisableWifi() {
		return disableWifi;
	}

	public void setDisableWifi(String disableWifi) {
		this.disableWifi = disableWifi;
	}

	/**
	 * Activate or not built-in android Car Mode
	 * 
	 * @return
	 */
	public boolean isActivateCarMode() {
		return this.activateCarMode;
	}

	public void setActivateCarMode(boolean activate) {
		this.activateCarMode = activate;
	}

	/**
	 * Get auto answer mode
	 * 
	 * @return
	 */
	public String getAutoAnswer() {
		return autoAnswer;
	}

	public void setAutoAnswer(String autoAnswer) {
		this.autoAnswer = autoAnswer;
	}

	/**
	 * @return the autorunApp
	 */
	public ComponentName getAutorunApp() {
		return autorunApp;
	}

	/**
	 * @param autorunApp
	 *            the autorunApp to set
	 */
	public void setAutorunApp(ComponentName autorunApp) {
		this.autorunApp = autorunApp;
	}

	public boolean isSamsungDrivingMode() {
		return samsungDrivingMode;
	}

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

		boolean hasComponent = false;
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

    public boolean isDisableScreenTimeoutCharging() {
        return disableScreenTimeoutCharging;
    }

    public void setDisableScreenTimeoutCharging(boolean disableScreenTimeoutCharging) {
        this.disableScreenTimeoutCharging = disableScreenTimeoutCharging;
    }
}