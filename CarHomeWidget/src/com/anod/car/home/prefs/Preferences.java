package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.HashMap;

public class Preferences {
	private Main main;
	private InCar incar;
	
	public Main getMain() {
		if (main == null) {
			this.main = new Main();
		}
		return main;
	}

	public InCar getIncar() {
		if (incar == null) {
			this.incar = new InCar();
		}
		return incar;
	}

	public class Main {
		private ArrayList<Long> launcherComponents;
		private String skin;		
		private Integer tileColor;
		private boolean iconsMono;
		private Integer iconsColor;
		private String iconsScale;
		private int fontColor;
		private int fontSize;
		private int backgroundColor;
		private boolean settingsTransparent;
		private boolean incarTransparent;
		
		public boolean isSettingsTransparent() {
			return settingsTransparent;
		}
		public boolean isIncarTransparent() {
			return incarTransparent;
		}
		public void setSettingsTransparent(boolean settingsTransparent) {
			this.settingsTransparent = settingsTransparent;
		}
		public void setIncarTransparent(boolean incarTransparent) {
			this.incarTransparent = incarTransparent;
		}
		public ArrayList<Long> getLauncherComponents() {
			return launcherComponents;
		}
		public String getSkin() {
			return skin;
		}
		public Integer getTileColor() {
			return tileColor;
		}
		public boolean isIconsMono() {
			return iconsMono;
		}
		public Integer getIconsColor() {
			return iconsColor;
		}
		public String getIconsScale() {
			return iconsScale;
		}
		public int getFontColor() {
			return fontColor;
		}
		public int getFontSize() {
			return fontSize;
		}
		public int getBackgroundColor() {
			return backgroundColor;
		}
		public void setLauncherComponents(ArrayList<Long> launcherComponents) {
			this.launcherComponents = launcherComponents;
		}
		public void setSkin(String skin) {
			this.skin = skin;
		}
		public void setTileColor(Integer tileColor) {
			this.tileColor = tileColor;
		}
		public void setIconsMono(boolean iconsMono) {
			this.iconsMono = iconsMono;
		}
		public void setIconsColor(Integer iconsColor) {
			this.iconsColor = iconsColor;
		}
		public void setIconsScaleString(String iconsScale) {
			this.iconsScale = iconsScale;
		}
		public void setFontColor(int fontColor) {
			this.fontColor = fontColor;
		}
		public void setFontSize(int fontSize) {
			this.fontSize = fontSize;
		}
		public void setBackgroundColor(int backgroundColor) {
			this.backgroundColor = backgroundColor;
		}
	}
	
	public class InCar {
		private boolean inCarEnabled;
		private boolean powerRequired;
		private boolean headsetRequired;
		private boolean bluetoothRequired = false;
		private HashMap<String,String> btDevices;
		
		private boolean disableBluetoothOnPower;
		private boolean enableBluetoothOnPower;
		
		private boolean  disableScreenTimeout;
		private boolean adjustVolumeLevel;
		private int mediaVolumeLevel;
		private boolean enableBluetooth;
		private String brightness;
		private String disableWifi;
		private boolean autoSpeaker;
		private boolean activateCarMode;
		
		
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
}
