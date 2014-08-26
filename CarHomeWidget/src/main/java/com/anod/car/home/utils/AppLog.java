package com.anod.car.home.utils;

import android.text.format.Time;
import android.util.Log;

import com.anod.car.home.BuildConfig;

public class AppLog {

	public static final String TAG = "CarHomeWidget";

	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, format(msg));
		}
	}

	public static void v(String msg) {
		//Log.v(TAG, format(msg));
		Log.v(TAG, format(msg));
	}
	
	public static void e(String msg) {
		//Log.e(TAG, format(msg));
		Log.e(TAG, format(msg));
	}

	public static void ex(Throwable tr) {
		Log.e(TAG, format(tr.getMessage()), tr);
	}

	public static void w(String msg) {
		String formatted = format(msg);
		Log.w(TAG, formatted);
	}
    /**
     * Format given time for debugging output.
     *
     * @param msg Current system time from {@link System#currentTimeMillis()}
     *            for calculating time difference.
     */
    public static String format(String msg ) {
    	long unixTime = System.currentTimeMillis();
        Time time = new Time();
        time.set(unixTime);

		//String formatTime = (timeOnly) ? "%d-%m-%Y %H:%M:%S" : "%M:%S";

        return String.format("[%s.%s] %s ", time.format("%M:%S"), String.valueOf(unixTime % 1000), msg);
    }	
}
