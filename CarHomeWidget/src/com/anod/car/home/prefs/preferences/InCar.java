package com.anod.car.home.prefs.preferences;

import java.util.HashMap;

import com.anod.car.home.prefs.PreferencesStorage;

public class InCar {
	private boolean inCarEnabled = false;
	private boolean powerRequired = false;
	private boolean headsetRequired = false;
	private boolean bluetoothRequired = false;
	private HashMap<String,String> btDevices = null;
	
	private boolean disableBluetoothOnPower = false;
	private boolean enableBluetoothOnPower = false;
	
	private boolean disableScreenTimeout = false;
	private boolean adjustVolumeLevel = false;
	private int mediaVolumeLevel = 100;
	private boolean enableBluetooth = false;
	private String brightness = PreferencesStorage.BRIGHTNESS_AUTO;
	private String disableWifi = PreferencesStorage.WIFI_NOACTION;
	private boolean autoSpeaker = false;
	private boolean activateCarMode = false;
	
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
	 * @param inCarEnabled the inCarEnabled to set
	 */
	public void setInCarEnabled(boolean incarEnabled) {
		this.inCarEnabled = incarEnabled;
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
	 * Map of addresses of bluetooth device on connect with one of them
	 * enable in car mode
	 * @return HashMap<Address,Address>
	 */
	public HashMap<String, String> getBtDevices() {
		return btDevices;
	}
	public void setBtDevices(HashMap<String, String> btDevices) {
		this.btDevices = btDevices;
		this.bluetoothRequired = (btDevices != null && btDevices.size() > 0);
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
	/**
	 * Level 0-100 of media volume
	 * @return
	 */
	public int getMediaVolumeLevel() {
		return mediaVolumeLevel;
	}
	public boolean isEnableBluetooth() {
		return enableBluetooth;
	}
	/**
	 * Get brightness preset
	 * 	@see PreferenceStorage.BRIGHTNESS_DEFAULT
	 * 	@see PreferenceStorage.BRIGHTNESS_AUTO
	 * 	@see PreferenceStorage.BRIGHTNESS_DAY
	 *  @see PreferenceStorage.BRIGHTNESS_NIGHT
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
	 * @see PreferenceStorage.WIFI_NOACTION
	 * @see PreferenceStorage.WIFI_TURNOFF
	 * @see PreferenceStorage.WIFI_DISABLE
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
	 * @return
	 */
	public boolean activateCarMode() {
		return this.activateCarMode;
	}
	public void setActivateCarMode(boolean activate) {
		this.activateCarMode = activate;
	}
}