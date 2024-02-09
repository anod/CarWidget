package info.anodsplace.carwidget.content.extentions

import android.app.ActivityManager
import android.content.Context
import info.anodsplace.carwidget.content.BuildProperties
import org.koin.java.KoinJavaComponent.getKoin

val Context.isLowMemoryDevice: Boolean
    get() {
        val activityManager = (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        return activityManager.isLowRamDevice
    }

val Context.isDebugBuild: Boolean
    get() = getKoin().get<BuildProperties>().isDebug

//val Context.isHighPerformingDevice: Boolean
//    get() {
//        val activityManager = (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
//        return BuildConfig.DEBUG || (!activityManager.isLowRamDevice
//        && (Runtime.getRuntime().availableProcessors() >= 4)
//        && activityManager.memoryClass >= 128)
//    }

@Suppress("DEPRECATION") // Deprecated for third party Services.
fun <T> Context.isServiceRunning(service: Class<T>) =
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.name }