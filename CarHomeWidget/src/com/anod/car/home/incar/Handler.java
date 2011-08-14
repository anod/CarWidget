package com.anod.car.home.incar;

import java.util.HashMap;

import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.preferences.InCar;


public class Handler {
	private static final int BRIGHTNESS_MAX = 255;
	private static final int BRIGHTNESS_NIGHT = 30;
	private static final int BRIGHTNESS_DAY = BRIGHTNESS_MAX;
	
	private static final byte FLAG_POWER = 0;
	private static final byte FLAG_HEADSET = 1;
	private static final byte FLAG_BLUETOOTH = 2;
	
	private static boolean[] sPrefState = {false,false,false};
	private static boolean[] sEventState = {false,false,false};

	private static boolean sMode = false;	
	private static boolean sWakeLocked = false;
	private static int mCurrentBtState;
	private static int mCurrentWiFiState;
	private static int mCurrentVolume;
	private static int mCurrentBrightness;
	private static boolean mCurrentAutoBrightness;
	private static PowerManager.WakeLock mWakeLock;
		
	public static void onBroadcastReceive(Context context, Intent intent) {
		if (!PreferencesStorage.isInCarModeEnabled(context)) { // TODO remove it
			return;
		}
		InCar prefs = PreferencesStorage.loadInCar(context);
		if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
		) {
			onPowerDisconnected(prefs);
		} else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
			onPowerConnected(prefs);
		}
				
		updatePrefState(prefs);
		updateEventState(prefs,intent);
		for (int i=0; i<3; i++) {
			Log.d("CarHomeWidget", "Pref state [" +i +"] : " + sPrefState[i]);	
			Log.d("CarHomeWidget", "Event state [" +i +"] : " + sEventState[i]);	
		}

		boolean newMode = detectNewMode();
		Log.d("CarHomeWidget", "New mode : " +newMode +" Car Mode:" + ModeService.sInCarMode);
		if (ModeService.sInCarMode == false && newMode == true) {
	    	Intent service = new Intent(context, ModeService.class);
	    	context.startService(service);
		} else if (ModeService.sInCarMode == true && newMode == false) {
	    	Intent service = new Intent(context, ModeService.class);
	    	context.stopService(service);
		}
	}
	
	private static void updatePrefState(InCar prefs) {
		sPrefState[FLAG_POWER] = isPlugRequired(FLAG_POWER,prefs);
		sPrefState[FLAG_BLUETOOTH] = isPlugRequired(FLAG_BLUETOOTH,prefs);
		sPrefState[FLAG_HEADSET] = isPlugRequired(FLAG_HEADSET,prefs);
	}
	
	public static void forceState(InCar prefs, boolean forceMode) {
		updatePrefState(prefs);
		if (sPrefState[FLAG_POWER]) {
			sEventState[FLAG_POWER] = forceMode;
		}
		if (sPrefState[FLAG_HEADSET]) {
			sEventState[FLAG_HEADSET] = forceMode;
		}
		if (sPrefState[FLAG_BLUETOOTH]) {
			sEventState[FLAG_BLUETOOTH] = forceMode;
		}		
	}
	
	private static boolean isPlugRequired(byte flag, InCar prefs) {
		switch(flag) {
			case FLAG_POWER:
				return prefs.isPowerRequired();
			case FLAG_BLUETOOTH:
				return prefs.isBluetoothRequired();
			case FLAG_HEADSET:
				return prefs.isHeadsetRequired();
		}
		throw new IllegalArgumentException("Unsupported");
	}
	
	private static void updateEventState(InCar prefs, Intent intent ) {
		String action = intent.getAction();
		if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
			sEventState[FLAG_POWER] = false;
			return;
		} 
		if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
			sEventState[FLAG_POWER] = true;
			return;
		}
		if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
			if (intent.getIntExtra("state",0) == 0) {
				sEventState[FLAG_HEADSET] = false;
			} else {
				sEventState[FLAG_HEADSET] = true;
			}
			return;
		}
		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			HashMap<String, String> devices = prefs.getBtDevices();
			if (devices != null ) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (devices.containsKey(device.getAddress())) {
					sEventState[FLAG_BLUETOOTH] = true;
					return;
				}
			}
			return;
		}
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
				sEventState[FLAG_BLUETOOTH] = false;
			}
			return;
		}
		if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			HashMap<String, String> devices = prefs.getBtDevices();
			if (devices != null ) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (devices.containsKey(device.getAddress())) {
					sEventState[FLAG_BLUETOOTH] = false;
					return;
				}
			}
			return;
		}
		
	}

	private static boolean detectNewMode() {
		boolean newMode = sMode;
		for (int i=0;i<sPrefState.length;i++) {
			if (sPrefState[i] == true) {
				newMode = true;
				if (sEventState[i] == false) {
					return false;
				}
			}
		}
		return newMode;
	}
	
	private static void onPowerConnected(InCar prefs) {
		if (prefs.isEnableBluetoothOnPower()) {
			if (Bluetooth.getState() != BluetoothAdapter.STATE_ON) {
				Bluetooth.switchOn();
			}
		}
	}

	private static void onPowerDisconnected(InCar prefs) {
		if (prefs.isDisableBluetoothOnPower()) {
			if (Bluetooth.getState() != BluetoothAdapter.STATE_OFF) {
				Bluetooth.switchOff();
			}
		}
	}
	
	public static void switchOn(InCar prefs, Context context) {
		sMode = true;
		if (prefs.isDisableScreenTimeout()) {
			acquireWakeLock(context);
		}
		if (prefs.isAdjustVolumeLevel()) {
			adjustVolume(prefs,context);
		}
		if (prefs.isEnableBluetooth()) {
			enableBluetooth();
		}
		if (!prefs.getDisableWifi().equals(PreferencesStorage.WIFI_NOACTION)) {
			disableWifi(context);
		}
		if (prefs.activateCarMode()) {
			activateCarMode(context);
		}
		String brightSetting = prefs.getBrightness();
		if (brightSetting != PreferencesStorage.BRIGHTNESS_DEFAULT) {
			adjustBrightness(brightSetting,context);
		}
	}	
	
	public static void switchOff(InCar prefs, Context context) {
		sMode = false;
		if (prefs.isDisableScreenTimeout()) {
			releaseWakeLock();
		}
		if (prefs.isAdjustVolumeLevel()) {
			restoreVolume(context);
		}
		if (prefs.isEnableBluetooth()) {
			restoreBluetooth();
		}
		if (prefs.getDisableWifi().equals(PreferencesStorage.WIFI_TURNOFF)) {
			restoreWiFi(context);
		}
		if (prefs.activateCarMode()) {
			deactivateCarMode(context);
		}
		String brightSetting = prefs.getBrightness();
		if (brightSetting != PreferencesStorage.BRIGHTNESS_DEFAULT) {
			restoreBrightness(brightSetting,context);
		}
	}

	private static void activateCarMode(Context context) {
		UiModeManager ui = (UiModeManager)context.getSystemService(Context.UI_MODE_SERVICE);
		ui.enableCarMode(0);
	}

	private static void deactivateCarMode(Context context) {
		UiModeManager ui = (UiModeManager)context.getSystemService(Context.UI_MODE_SERVICE);
		ui.disableCarMode(0);
	}
	
	private static void disableWifi(Context context) {
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		mCurrentWiFiState = wm.getWifiState();
		if (mCurrentWiFiState != WifiManager.WIFI_STATE_DISABLED) {
			wm.setWifiEnabled(false);
		}
	}
	
	private static void restoreWiFi(Context context) {
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if (mCurrentWiFiState == WifiManager.WIFI_STATE_ENABLED) {
			wm.setWifiEnabled(true);
		}
	}
	
	private static void enableBluetooth() {
		mCurrentBtState = Bluetooth.getState(); 
		if (mCurrentBtState != BluetoothAdapter.STATE_ON) {
			Bluetooth.switchOn();
		}
	}

	private static void restoreBluetooth() {
		if (mCurrentBtState != BluetoothAdapter.STATE_ON) {
			Bluetooth.switchOff();
		}
	}

	private static void acquireWakeLock(Context context) {
		PowerManager pw = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pw.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "CarHomeWidget");

		if (mWakeLock != null && !sWakeLocked && !mWakeLock.isHeld()) {
			mWakeLock.acquire();
			sWakeLocked = true;
		}
		 
	}
	
	private static void adjustBrightness(String brightSetting,Context context) {
		ContentResolver cr = context.getContentResolver();
		
		mCurrentBrightness = android.provider.Settings.System.getInt(cr, 
			android.provider.Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_MAX
		);
		mCurrentAutoBrightness = (android.provider.Settings.System.getInt(cr, 
			android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
			android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
		) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		
		int newBrightLevel = -1;
		int newBrightMode = -1;
		if (PreferencesStorage.BRIGHTNESS_AUTO.equals(brightSetting)) {
			if (!mCurrentAutoBrightness) {
				newBrightLevel = mCurrentBrightness;
				newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			}
		} else if (PreferencesStorage.BRIGHTNESS_DAY.equals(brightSetting)) {
			newBrightLevel = BRIGHTNESS_DAY;
			newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
		} else if (PreferencesStorage.BRIGHTNESS_NIGHT.equals(brightSetting)) {
			newBrightLevel = BRIGHTNESS_NIGHT;
			newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;			
		}
		
		if (newBrightLevel == -1) {
			Log.d("CarHomeWidget", "Wrong brightness setting Mode : "+brightSetting+" Level : "+newBrightLevel);
			return;
		}
		
		if (newBrightMode!=-1) {
			android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode);
		}
		
		android.provider.Settings.System.putInt(cr, 
                 android.provider.Settings.System.SCREEN_BRIGHTNESS, newBrightLevel);
					
		setBrightnessIntent(newBrightLevel, context);            
    }

	private static boolean restoreBrightness(String brightSetting,Context context) {
		ContentResolver cr = context.getContentResolver();
		if (mCurrentAutoBrightness && PreferencesStorage.BRIGHTNESS_AUTO.equals(brightSetting)) {
			return false;
		}
		int newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
		if (mCurrentAutoBrightness) {
			newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		}
		android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode);
		android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, mCurrentBrightness);
		
		setBrightnessIntent(mCurrentBrightness, context);
		return true;
	}
	
	private static void adjustVolume(InCar prefs,Context context) {
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int adjVolume = prefs.getMediaVolumeLevel();
		int volume = PreferencesStorage.DEFAULT_VOLUME_LEVEL;
		if (adjVolume != PreferencesStorage.DEFAULT_VOLUME_LEVEL) {
			int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			volume = (int)((maxVolume*adjVolume)/100);
		}
		mCurrentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);
	}

	private static void restoreVolume(Context context) {
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC,mCurrentVolume,0);
	}
	
	private static void releaseWakeLock() {
		if (mWakeLock != null && sWakeLocked && mWakeLock.isHeld()) {
			mWakeLock.release();
			sWakeLocked = false;
		}
		mWakeLock = null;
	}
	
	private static void setBrightnessIntent(int newBrightLevel, Context context) {
		Intent intent = new Intent(context, ChangeBrightnessActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		float bt = (float)newBrightLevel/BRIGHTNESS_MAX;
    	intent.putExtra(ChangeBrightnessActivity.EXTRA_BRIGHT_LEVEL, bt);
		context.startActivity(intent);    	
	}

}
