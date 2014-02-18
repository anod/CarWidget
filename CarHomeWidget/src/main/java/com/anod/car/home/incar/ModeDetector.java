package com.anod.car.home.incar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.PowerUtil;

import java.util.HashMap;

/**
 * @author alex
 * @date 12/25/13
 */
public class ModeDetector {
	public static final byte FLAG_POWER = 0;
	public static final byte FLAG_HEADSET = 1;
	public static final byte FLAG_BLUETOOTH = 2;
	public static final byte FLAG_ACTIVITY = 3;

	private static boolean[] sPrefState = {false, false, false, false};
	private static boolean[] sEventState = {false, false, false, false};

	private static boolean sMode;

	public static boolean getEventState(int flag) {
		return sEventState[flag];
	}

	public static void onRegister(Context context) {
		sEventState[FLAG_POWER] = PowerUtil.isConnected(context);

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
		updateEventState(prefs, intent);
		if (BuildConfig.DEBUG) {
			for (int i = 0; i < sPrefState.length; i++) {
				AppLog.d("Pref state [" + i + "] : " + sPrefState[i]);
				AppLog.d("Event state [" + i + "] : " + sEventState[i]);
			}
		}

		boolean newMode = detectNewMode();
		AppLog.d("New mode : " + newMode + " Car Mode:" + ModeService.sInCarMode);
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
				Handler.adjustVolume(prefs, context);
			}
		}
	}




	private static void updateEventState(InCar prefs, Intent intent) {
		String action = intent.getAction();

		if (ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION.equals(action)) {
			sEventState[FLAG_ACTIVITY] = ActivityRecognitionHelper.checkCarState(intent, sEventState[FLAG_ACTIVITY]);
			return;
		}

		if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
			sEventState[FLAG_POWER] = true;
			return;
		}

		if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
			sEventState[FLAG_POWER] = false;
			return;
		}

		if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
			if (intent.getIntExtra("state", 0) == 0) {
				sEventState[FLAG_HEADSET] = false;
			} else {
				sEventState[FLAG_HEADSET] = true;
			}
			return;
		}
		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			HashMap<String, String> devices = prefs.getBtDevices();
			if (devices != null) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (devices.containsKey(device.getAddress())) {
					sEventState[FLAG_BLUETOOTH] = true;
					return;
				}
			}
			return;
		}
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
				sEventState[FLAG_BLUETOOTH] = false;
			}
			return;
		}
		if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			HashMap<String, String> devices = prefs.getBtDevices();
			if (devices != null) {
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
		for (int i = 0; i < sPrefState.length; i++) {
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

	/**
	 * reset activity state flag
	 */
	private static void resetActivityState() {
		sEventState[FLAG_ACTIVITY] = false;
		ActivityRecognitionService.resetLastResult();
	}

	public static void switchOn(InCar prefs, Context context) {
		sMode = true;
		Handler.enable(prefs, context);
	}

	public static void switchOff(InCar prefs, Context context) {
		sMode = false;
		resetActivityState();

		Handler.disable(prefs, context);
	}
}