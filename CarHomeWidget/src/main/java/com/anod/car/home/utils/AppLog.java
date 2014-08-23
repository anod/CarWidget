package com.anod.car.home.utils;

import android.text.format.Time;
import android.util.Log;

import com.anod.car.home.BuildConfig;

public class AppLog {

	public static final String TAG = "CarHomeWidget";
	private final static Object sLock = new Object();

	public static class Entry {
		public String msg;
		public int level;

		Entry(int level, String msg) {
			this.level = level;
			this.msg = msg;
		}
	}

	public interface LogListener {
		void onMessage(final AppLog.Entry entry);
	}

	private static LogListener sListener;

	public static void setListener(LogListener listener) {
		synchronized (sLock) {
			sListener = listener;
			if (sListener == null) {
				return;
			}
		}
	}

	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, format(msg));
			notifyMessage(Log.DEBUG, format(msg));
		}
	}

	public static void v(String msg) {
		//Log.v(TAG, format(msg));
		Log.v(TAG, format(msg));
		notifyMessage(Log.VERBOSE, format(msg));
	}
	
	public static void e(String msg) {
		//Log.e(TAG, format(msg));
		Log.e(TAG, format(msg));
		notifyMessage(Log.ERROR, format(msg));
	}

	public static void ex(Throwable tr) {
		Log.e(TAG, format(tr.getMessage()), tr);
		notifyMessage(Log.ERROR, tr.getStackTrace().toString());
	}

	private static void notifyMessage(int level, String msg) {
		synchronized (sLock) {
			if (sListener != null) {
				sListener.onMessage(new Entry(level, msg));
			}
		}
	}


	public static void w(String msg) {
		String formatted = format(msg);
		Log.w(TAG, formatted);
		notifyMessage(Log.WARN, formatted);
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
