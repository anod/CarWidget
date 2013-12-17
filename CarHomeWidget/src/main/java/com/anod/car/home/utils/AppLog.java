package com.anod.car.home.utils;

import android.text.format.Time;
import android.util.Log;

import com.anod.car.home.BuildConfig;
import com.crashlytics.android.Crashlytics;

public class AppLog {

	public static final String TAG = "CarHomeWidget";


	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			Crashlytics.log(Log.DEBUG, TAG, format(msg));
		}
	}
	
	public static void v(String msg) {
		//Log.v(TAG, format(msg));
		Crashlytics.log(Log.VERBOSE, TAG, format(msg));
	}
	
	public static void e(String msg) {
		//Log.e(TAG, format(msg));
		Crashlytics.log(Log.ERROR, TAG, format(msg));
	}

	public static void ex(Throwable tr) {
		//Log.e(TAG, format(msg), tr);
		Crashlytics.logException(tr);
	}

	public static void w(String msg) {
		Crashlytics.log(Log.WARN, TAG, format(msg));
		//Log.w(TAG, format(msg));
	}
    /**
     * Format given time for debugging output.
     *
     * @param msg Current system time from {@link System#currentTimeMillis()}
     *            for calculating time difference.
     */
    public static String format(String msg) {
    	long unixTime = System.currentTimeMillis();
        Time time = new Time();
        time.set(unixTime);

        return String.format("[%s] %s ", time.format("%d-%m-%Y %H:%M:%S"), msg);
    }	
}
