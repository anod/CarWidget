package com.anod.car.home.incar;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * @author alex
 * @date 9/19/13
 */
public class SamsungDrivingMode {

	public static final String DRIVING_MODE_ON = "driving_mode_on";
	public static final String DEVICE_SAMSUNG = "samsung";

	final public static boolean IS_SAMSUNG = (Build.MANUFACTURER.equals(DEVICE_SAMSUNG));

	public static boolean hasMode() {
		return IS_SAMSUNG;
	}

	public static boolean enabled(Context context) {
		int v = Settings.System.getInt(context.getContentResolver(), DRIVING_MODE_ON, 0);
		return v == 1;
	}

	public static void enable(Context context) {
		Settings.System.putInt(context.getContentResolver(), DRIVING_MODE_ON, 1);
	}

	public static void disable(Context context) {
		Settings.System.putInt(context.getContentResolver(), DRIVING_MODE_ON, 0);
	}

}
