package com.anod.car.home.incar;

import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.Utils;

public class Handler {
	private static final int VOLUME_NOT_SET = -1;
	private static final String TAG = "CarHomeWidget";
	private static final int BRIGHTNESS_MAX = 255;
	private static final int BRIGHTNESS_NIGHT = 30;
	private static final int BRIGHTNESS_DAY = BRIGHTNESS_MAX;
	
	private static final byte FLAG_POWER = 0;
	private static final byte FLAG_HEADSET = 1;
	private static final byte FLAG_BLUETOOTH = 2;
	private static final byte FLAG_ACTIVITY = 3;

	private static boolean[] sPrefState = {false,false,false,false};
	private static boolean[] sEventState = {false,false,false,false};

	private static boolean sMode;
	private static boolean sWakeLocked;
	private static int sCurrentBtState;
	private static int sCurrentWiFiState;
	private static int sCurrentMediaVolume = VOLUME_NOT_SET;
	private static int sCurrentCallVolume = VOLUME_NOT_SET;

	private static int sCurrentBrightness;
	private static boolean sCurrentAutoBrightness;
	private static PowerManager.WakeLock sWakeLock;
	/**
	 * For thread safety
	 */
	private static final Object[] LOCK = new Object[0];

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
		if (BuildConfig.DEBUG) {
			for (int i=0; i<sPrefState.length; i++) {
				Log.d(TAG, "Pref state [" +i +"] : " + sPrefState[i]);
				Log.d(TAG, "Event state [" +i +"] : " + sEventState[i]);
			}
		}

		boolean newMode = detectNewMode();
		Log.d(TAG, "New mode : " +newMode +" Car Mode:" + ModeService.sInCarMode);
		if (!ModeService.sInCarMode && newMode) {
	    	Intent service = new Intent(context, ModeService.class);
	    	context.startService(service);
		} else if (ModeService.sInCarMode && !newMode) {
	    	Intent service = new Intent(context, ModeService.class);
	    	context.stopService(service);
		}
		
		String action = intent.getAction();
		boolean inCarEnabled = ModeService.sInCarMode && newMode;
		if (inCarEnabled && BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			if (prefs.isAdjustVolumeLevel()) {
				adjustVolume(prefs,context);
			}
		}
	}
	
	private static void updatePrefState(InCar prefs) {
		sPrefState[FLAG_POWER] = prefs.isPowerRequired();
		sPrefState[FLAG_BLUETOOTH] = prefs.isBluetoothRequired();
		sPrefState[FLAG_HEADSET] = prefs.isHeadsetRequired();
		sPrefState[FLAG_ACTIVITY] = prefs.isActivityRequired();
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
		if (sPrefState[FLAG_ACTIVITY]) {
			sEventState[FLAG_ACTIVITY] = forceMode;
		}
	}

	
	private static void updateEventState(InCar prefs, Intent intent ) {
		String action = intent.getAction();

		if (ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION.equals(action)) {
			sEventState[FLAG_ACTIVITY] = ActivityRecognitionHelper.checkCarState(intent, sEventState[FLAG_ACTIVITY]);
			return;
		}

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
			if (sPrefState[i]) {
				newMode = true;
				if (!sEventState[i]) {
					return false;
				}
			}
		}
		return newMode;
	}
	
	private static void onPowerConnected(InCar prefs) {
		if (prefs.isEnableBluetoothOnPower() && Bluetooth.getState() != BluetoothAdapter.STATE_ON) {
			Bluetooth.switchOn();
		}
	}

	private static void onPowerDisconnected(InCar prefs) {
		if (prefs.isDisableBluetoothOnPower() && Bluetooth.getState() != BluetoothAdapter.STATE_OFF) {
			Bluetooth.switchOff();
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
		if (!prefs.getDisableWifi().equals(InCar.WIFI_NOACTION)) {
			disableWifi(context);
		}
		if (prefs.isActivateCarMode()) {
			activateCarMode(context);
		}

		if (SamsungDrivingMode.hasMode() && prefs.isSamsungDrivingMode()) {
			SamsungDrivingMode.enable(context);
		}

		ComponentName autorunApp = prefs.getAutorunApp();
		if (autorunApp != null) {
			runApp(autorunApp,context);
		}
		String brightSetting = prefs.getBrightness();
		if (!brightSetting.equals(InCar.BRIGHTNESS_DISABLED)) {
			adjustBrightness(brightSetting,context);
		}
	}	
	
	private static void runApp(ComponentName autorunApp, Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN)
			.setComponent(autorunApp)
			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Utils.startActivitySafely(intent,context);		
	}

	public static void switchOff(InCar prefs, Context context) {
		sMode = false;

		resetActivityState();

		if (prefs.isDisableScreenTimeout()) {
			releaseWakeLock();
		}
		if (prefs.isAdjustVolumeLevel()) {
			restoreVolume(context);
		}
		if (prefs.isEnableBluetooth()) {
			restoreBluetooth();
		}
		if (prefs.getDisableWifi().equals(InCar.WIFI_TURNOFF)) {
			restoreWiFi(context);
		}
		if (prefs.isActivateCarMode()) {
			deactivateCarMode(context);
		}

		if (SamsungDrivingMode.hasMode() && prefs.isSamsungDrivingMode()) {
			SamsungDrivingMode.disable(context);
		}

		String brightSetting = prefs.getBrightness();
		if (!brightSetting.equals(InCar.BRIGHTNESS_DISABLED)) {
			restoreBrightness(brightSetting,context);
		}
	}

	/**
	 * reset activity state flag
	 */
	private static void resetActivityState() {
		sEventState[FLAG_ACTIVITY] = false;
		ActivityRecognitionService.resetLastResult();
	}


	@TargetApi(Build.VERSION_CODES.FROYO)
	private static void activateCarMode(Context context) {
		UiModeManager ui = (UiModeManager)context.getSystemService(Context.UI_MODE_SERVICE);
		ui.enableCarMode(0);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private static void deactivateCarMode(Context context) {
		UiModeManager ui = (UiModeManager)context.getSystemService(Context.UI_MODE_SERVICE);
		ui.disableCarMode(0);
	}
	
	private static void disableWifi(Context context) {
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		sCurrentWiFiState = wm.getWifiState();
		if (sCurrentWiFiState != WifiManager.WIFI_STATE_DISABLED) {
			wm.setWifiEnabled(false);
		}
	}
	
	private static void restoreWiFi(Context context) {
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if (sCurrentWiFiState == WifiManager.WIFI_STATE_ENABLED) {
			wm.setWifiEnabled(true);
		}
	}
	
	private static void enableBluetooth() {
		sCurrentBtState = Bluetooth.getState();
		if (sCurrentBtState != BluetoothAdapter.STATE_ON) {
			Bluetooth.switchOn();
		}
	}

	private static void restoreBluetooth() {
		if (sCurrentBtState != BluetoothAdapter.STATE_ON) {
			Bluetooth.switchOff();
		}
	}

	@SuppressWarnings("deprecation")
	private static void acquireWakeLock(Context context) {
		PowerManager pw = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		sWakeLock = pw.newWakeLock(
			PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
			PowerManager.ACQUIRE_CAUSES_WAKEUP
		, TAG);

		synchronized(LOCK) {
			if (sWakeLock != null && !sWakeLocked && !sWakeLock.isHeld()) {
				sWakeLock.acquire();
				sWakeLocked = true;
			}
		}
	}
	
	private static void adjustBrightness(String brightSetting,Context context) {
		ContentResolver cr = context.getContentResolver();
		
		sCurrentBrightness = android.provider.Settings.System.getInt(cr,
			android.provider.Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_MAX
		);
		sCurrentAutoBrightness = (android.provider.Settings.System.getInt(cr,
			android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
			android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
		) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		
		int newBrightLevel = -1;
		int newBrightMode = -1;
		if (InCar.BRIGHTNESS_AUTO.equals(brightSetting)) {
			if (!sCurrentAutoBrightness) {
				newBrightLevel = sCurrentBrightness;
				newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			}
		} else if (InCar.BRIGHTNESS_DAY.equals(brightSetting)) {
			newBrightLevel = BRIGHTNESS_DAY;
			newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
		} else if (InCar.BRIGHTNESS_NIGHT.equals(brightSetting)) {
			newBrightLevel = BRIGHTNESS_NIGHT;
			newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;			
		}
		
		if (newBrightLevel == -1) {
			Log.d(TAG, "Wrong brightness setting Mode : "+brightSetting+" Level : "+newBrightLevel);
			return;
		}
		
		if (newBrightMode!=-1) {
			android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode);
		}
		
		android.provider.Settings.System.putInt(cr, 
                 android.provider.Settings.System.SCREEN_BRIGHTNESS, newBrightLevel);
					
		sendBrightnessIntent(newBrightLevel, context);            
    }

	private static boolean restoreBrightness(String brightSetting,Context context) {
		if (sCurrentAutoBrightness && InCar.BRIGHTNESS_AUTO.equals(brightSetting)) {
			return false;
		}
		int newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
		if (sCurrentAutoBrightness) {
			newBrightMode = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		}
		ContentResolver cr = context.getContentResolver();
		android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, newBrightMode);
		android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, sCurrentBrightness);
		
		sendBrightnessIntent(sCurrentBrightness, context);
		return true;
	}
	
	
	private static void adjustVolume(InCar prefs,Context context) {
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int adjVolume = prefs.getMediaVolumeLevel();
		int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int volume = (int)((maxVolume*adjVolume)/100);
		
		if (sCurrentMediaVolume == VOLUME_NOT_SET) {
			sCurrentMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		}

		audio.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);

		int adjCallVolume = prefs.getCallVolumeLevel();
		int maxCallVolume = audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		int callVolume = (int)((maxCallVolume*adjCallVolume)/100);

		if (sCurrentCallVolume == VOLUME_NOT_SET) {
			sCurrentCallVolume = audio.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		}

		audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL,callVolume,0);
	}


	private static void restoreVolume(Context context) {
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		if (sCurrentMediaVolume != VOLUME_NOT_SET) {
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, sCurrentMediaVolume, 0);
			sCurrentMediaVolume = VOLUME_NOT_SET;
		}

		if (sCurrentCallVolume != VOLUME_NOT_SET) {
			audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, sCurrentCallVolume, 0);
			sCurrentCallVolume = VOLUME_NOT_SET;
		}

	}
	
	private static void releaseWakeLock() {
		synchronized (LOCK) {
			if (sWakeLock != null && sWakeLocked && sWakeLock.isHeld()) {
				sWakeLock.release();
				sWakeLocked = false;
			}
			sWakeLock = null;
		}
	}
	
	private static void sendBrightnessIntent(int newBrightLevel, Context context) {
		Intent intent = new Intent(context, ChangeBrightnessActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		float bt = (float)newBrightLevel/BRIGHTNESS_MAX;
    	intent.putExtra(ChangeBrightnessActivity.EXTRA_BRIGHT_LEVEL, bt);
		context.startActivity(intent);
	}

}
