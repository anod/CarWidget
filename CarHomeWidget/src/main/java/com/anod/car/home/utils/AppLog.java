package com.anod.car.home.utils;

import com.anod.car.home.BuildConfig;

import android.text.format.Time;
import android.util.Log;

import java.util.IllegalFormatException;
import java.util.Locale;

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

    public static void e(String msg, final Object... params) {
        Log.e(TAG, format(msg, params));
    }

    public static void ex(Throwable tr) {
        Log.e(TAG, format(tr.getMessage()), tr);
    }

    public static void w(String msg) {
        String formatted = format(msg);
        Log.w(TAG, formatted);
    }

    private static String format(final String msg, final Object... array) {
        String formatted;
        if (array == null) {
            formatted = msg;
        } else {
            try {
                formatted = String.format(Locale.US, msg, array);
            } catch (IllegalFormatException ex) {
                e("IllegalFormatException: formatString='%s' numArgs=%d", msg, array.length);
                formatted = msg + " (An error occurred while formatting the message.)";
            }
        }
        final StackTraceElement[] stackTrace = new Throwable().fillInStackTrace().getStackTrace();
        String string = "<unknown>";
        for (int i = 2; i < stackTrace.length; ++i) {
            final String className = stackTrace[i].getClassName();
            if (!className.equals(AppLog.class.getName())) {
                final String substring = className.substring(1 + className.lastIndexOf(46));
                string = substring.substring(1 + substring.lastIndexOf(36)) + "." + stackTrace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), string,
                formatted);
    }
}
