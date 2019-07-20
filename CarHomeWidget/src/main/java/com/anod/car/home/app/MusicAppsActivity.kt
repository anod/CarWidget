package com.anod.car.home.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.collection.SimpleArrayMap
import com.anod.car.home.BuildConfig
import com.anod.car.home.model.AppsList
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * @date 2014-09-01
 */
abstract class MusicAppsActivity : AppsListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.appsList = AppsList()
        viewModel.loader = MediaListLoader(applicationContext)
    }

    class MediaListLoader(context: Context) : AppsListLoader {
        private val packageManager = context.packageManager

        override suspend fun loadAppsList(): List<AppsList.Entry> = withContext(Dispatchers.Default) {
            val apps = packageManager
                    .queryBroadcastReceivers(Intent(Intent.ACTION_MEDIA_BUTTON), PackageManager.GET_RESOLVED_FILTER)
            // filter duplicate receivers
            val receivers = SimpleArrayMap<String, Boolean>(apps.size)
            val list = mutableListOf<AppsList.Entry>()
            for (appInfo in apps) {
                val pkg = appInfo.activityInfo.packageName
                // App title
                if (sExcludePackages.contains(pkg) || receivers.containsKey(pkg)) {
                    continue
                }
                val title = appInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString()
                if (BuildConfig.DEBUG) {
                    AppLog.d(appInfo.activityInfo.packageName + "/"
                            + appInfo.activityInfo.applicationInfo.className)
                }
                receivers.put(pkg, true)
                list.add(AppsList.Entry(appInfo, title))
            }
            list.sortBy { it.title }
            return@withContext list
        }
    }

    companion object {
        private var sExcludePackages = setOf(
                "com.amazon.kindle",
                "com.google.android.apps.magazines",
                "flipboard.app",
                "com.sec.android.app.storycam",
                "com.sec.android.app.mediasync",
                "com.sec.android.mmapp",
                "com.sec.android.automotive.drivelink",
                "com.sec.android.app.mv.player",
                "com.sec.android.app.voicenote"
        )
    }
}
