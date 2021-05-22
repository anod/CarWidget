package info.anodsplace.carwidget.extentions

import android.app.ActivityManager
import android.content.Context
import info.anodsplace.carwidget.BuildConfig

val Context.isLowMemoryDevice: Boolean
    get() {
        val activityManager = (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        return activityManager.isLowRamDevice
    }

val Context.isHighPerformingDevice: Boolean
    get() {
        val activityManager = (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        return BuildConfig.DEBUG || (!activityManager.isLowRamDevice
        && (Runtime.getRuntime().availableProcessors() >= 4)
        && activityManager.memoryClass >= 128)
    }