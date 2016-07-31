package com.anod.car.home.utils;

import com.anod.car.home.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import info.anodsplace.android.log.AppLog;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

public class Utils {
    static final char CACHE_KEY_SEPARATOR = '\n';

    final public static boolean IS_ISC_MR1_OR_GREATER = (Build.VERSION.SDK_INT >= 15);

    final public static boolean IS_JELLYBEAN_OR_GREATER = (Build.VERSION.SDK_INT >= 16);

    final public static boolean IS_JELLYBEAN_MR2_OR_GREATER = (Build.VERSION.SDK_INT >= 18);
            // 4.3 JELLY_BEAN_MR2

    public static final boolean IS_KITKAT_OR_GREATER = (Build.VERSION.SDK_INT >= 19);


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isLowMemoryDevice()
    {
        if (IS_KITKAT_OR_GREATER) {
            ActivityManager.RunningAppProcessInfo memory = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(memory);

            return memory.lastTrimLevel >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW;
        }
        return true;
    }

    public static boolean isProInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.anod.car.home.pro", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isFreeInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.anod.car.home.free", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int readAppWidgetId(Bundle savedInstanceState, Intent launchIntent) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        } else {
            Bundle extras = launchIntent.getExtras();
            if (extras != null) {
                return extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        }
        return AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    public static void saveAppWidgetId(Bundle outState, int mAppWidgetId) {
        outState.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    }

    public static float calcIconsScale(String scaleString) {
        return 1.0f + 0.1f * Integer.valueOf(scaleString);
    }

    public static String componentToString(ComponentName component) {
        return component.getPackageName() + "/" + component.getClassName();
    }

    public static ComponentName stringToComponent(String compString) {
        String[] compParts = compString.split("/");
        return new ComponentName(compParts[0], compParts[1]);
    }

    public static void startActivityForResultSafetly(Intent intent, int requestCode,
            Activity activity) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(activity, R.string.photo_picker_not_found, Toast.LENGTH_LONG).show();
        } catch (Exception exception) {
            String errStr = String.format(activity.getResources().getString(R.string.error_text),
                    exception.getMessage());
            Toast.makeText(activity, errStr, Toast.LENGTH_LONG).show();
        }
    }

    public static void startActivitySafely(Intent intent, Context context) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.activity_not_found),
                    Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(context, context.getString(R.string.activity_not_found),
                    Toast.LENGTH_SHORT).show();
            AppLog.e("Widget does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.");
            AppLog.e(e);
        }
    }

    static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = getService(context, ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap) {
            memoryClass = am.getLargeMemoryClass();
        }
        // Target ~15% of the available heap.
        return 1024 * 1024 * memoryClass / 7;
    }

    @SuppressWarnings("unchecked")
    static <T> T getService(Context context, String service) {
        return (T) context.getSystemService(service);
    }
}
