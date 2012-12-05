package com.anod.car.home.incar;

import android.bluetooth.BluetoothAdapter;

public class Bluetooth {

	public static int getState() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			return BluetoothAdapter.STATE_OFF;
		}
		return btAdapter.getState();
	}
	
	public static void switchOn() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		btAdapter.enable();
		//btAdapter.startDiscovery();
	}
	
	public static void switchOff() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		btAdapter.disable();
	}
}
