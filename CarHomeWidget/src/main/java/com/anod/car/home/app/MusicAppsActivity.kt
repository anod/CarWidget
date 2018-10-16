package com.anod.car.home.app

import com.anod.car.home.BuildConfig
import com.anod.car.home.model.AppsList
import info.anodsplace.framework.AppLog

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle

import java.util.HashSet

/**
 * @author alex
 * @date 2014-09-01
 */
abstract class MusicAppsActivity : AppsListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.appsList = AppsList()
        viewModel.loaderFactory = { context, callback -> MediaAppsLoader(context, callback) }
    }

    internal class MediaAppsLoader(context: Context, private val callback: AppsListResultCallback) : AsyncTask<Void, Void, List<AppsList.Entry>>() {
        private val packageManager = context.packageManager

        override fun doInBackground(vararg params: Void?): List<AppsList.Entry> {
            val apps = packageManager
                    .queryBroadcastReceivers(Intent(Intent.ACTION_MEDIA_BUTTON), 96)
            // filter duplicate receivers
            val receivers = androidx.collection.SimpleArrayMap<String, Boolean>(apps.size)
            val list = mutableListOf<AppsList.Entry>()
            for (appInfo in apps) {
                val pkg = appInfo.activityInfo.packageName
                // App title
                if (sExcludePackages!!.contains(pkg) || receivers.containsKey(pkg)) {
                    continue
                }
                val title = appInfo.activityInfo.applicationInfo.loadLabel(packageManager)
                        .toString()
                if (BuildConfig.DEBUG) {
                    AppLog.d(appInfo.activityInfo.packageName + "/"
                            + appInfo.activityInfo.applicationInfo.className)
                }
                receivers.put(pkg, true)
                list.add(AppsList.Entry(appInfo, title))
            }
            list.sortBy { it.title }
            return list
        }

        override fun onPostExecute(result: List<AppsList.Entry>?) {
            super.onPostExecute(result)
            callback.onResult(result ?: emptyList())
        }
    }

    companion object {

        private var sExcludePackages: HashSet<String>? = null

        init {
            sExcludePackages = HashSet(2)
            sExcludePackages!!.add("com.amazon.kindle")
            sExcludePackages!!.add("com.google.android.apps.magazines")
            sExcludePackages!!.add("flipboard.app")
            // Samsung crap
            sExcludePackages!!.add("com.sec.android.app.storycam")
            sExcludePackages!!.add("com.sec.android.app.mediasync")
            sExcludePackages!!.add("com.sec.android.mmapp")
            sExcludePackages!!.add("com.sec.android.automotive.drivelink")
            sExcludePackages!!.add("com.sec.android.app.mv.player")
            sExcludePackages!!.add("com.sec.android.app.voicenote")

        }
    }
}
