package com.anod.car.home.utils

import com.anod.car.home.R

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent.getService
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentCallbacks2
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast

import info.anodsplace.android.log.AppLog

import android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP
import androidx.core.content.systemService

object Utils {

    val isLowMemoryDevice: Boolean
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val memory = ActivityManager.RunningAppProcessInfo()
                ActivityManager.getMyMemoryState(memory)

                return memory.lastTrimLevel >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
            }
            return true
        }

    fun isProInstalled(context: Context): Boolean {
        try {
            context.packageManager.getApplicationInfo("com.anod.car.home.pro", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }

    fun isFreeInstalled(context: Context): Boolean {
        try {
            context.packageManager.getApplicationInfo("com.anod.car.home.free", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }

    fun readAppWidgetId(savedInstanceState: Bundle?, launchIntent: Intent): Int {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
        } else {
            val extras = launchIntent.extras
            if (extras != null) {
                return extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID)
            }
        }
        return AppWidgetManager.INVALID_APPWIDGET_ID
    }

    fun saveAppWidgetId(outState: Bundle, mAppWidgetId: Int) {
        outState.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
    }

    fun calcIconsScale(scaleString: String): Float {
        return 1.0f + 0.1f * Integer.valueOf(scaleString)
    }


    fun stringToComponent(compString: String): ComponentName {
        val compParts = compString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return ComponentName(compParts[0], compParts[1])
    }

    fun startActivityForResultSafetly(intent: Intent, requestCode: Int,
                                      activity: Activity) {
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.photo_picker_not_found, Toast.LENGTH_LONG).show()
        } catch (exception: Exception) {
            val errStr = String.format(activity.resources.getString(R.string.error_text),
                    exception.message)
            Toast.makeText(activity, errStr, Toast.LENGTH_LONG).show()
        }

    }

    fun startActivitySafely(intent: Intent, context: Context) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.activity_not_found),
                    Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, context.getString(R.string.activity_not_found),
                    Toast.LENGTH_SHORT).show()
            AppLog.e("Widget does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.")
            AppLog.e(e)
        }

    }

    fun calculateMemoryCacheSize(context: Context): Int {
        val am: ActivityManager = context.systemService()
        val largeHeap = context.applicationInfo?.flags?.and(FLAG_LARGE_HEAP)
        val memoryClass = if (largeHeap != 0) am.largeMemoryClass else am.memoryClass
        // Target ~15% of the available heap.
        return 1024 * 1024 * memoryClass / 7
    }

}
