package info.anodsplace.carwidget.content.extentions

import android.app.ActivityManager
import android.content.Context
import info.anodsplace.carwidget.content.BuildConfig

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