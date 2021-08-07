package com.anod.car.home.utils

import android.annotation.TargetApi
import android.app.ActivityManager
import android.appwidget.AppWidgetManager
import android.content.ComponentCallbacks2
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

object Utils {

    fun isProInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getApplicationInfo("com.anod.car.home.pro", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isFreeInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getApplicationInfo("com.anod.car.home.free", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
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
}
