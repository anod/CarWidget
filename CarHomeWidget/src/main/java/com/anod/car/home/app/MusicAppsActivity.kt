package com.anod.car.home.app

import com.anod.car.home.BuildConfig
import com.anod.car.home.R
import com.anod.car.home.model.AppsList
import info.anodsplace.android.log.AppLog

import android.content.AsyncTaskLoader
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.os.Bundle
import android.support.v4.util.SimpleArrayMap

import java.util.ArrayList
import java.util.HashSet

/**
 * @author alex
 * @date 2014-09-01
 */
abstract class MusicAppsActivity : AppsListActivity() {

    override fun createAppList(context: Context): AppsList {
        return AppsList(App.get(context))
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<AppsList.Entry>> {
        return MediaAppsLoader(this, appsList)
    }

    internal class MediaAppsLoader(context: Context, private val mAppsList: AppsList) : AsyncTaskLoader<List<AppsList.Entry>>(context) {

        override fun loadInBackground(): ArrayList<AppsList.Entry> {
            val packageManager = context.packageManager
            val apps = packageManager
                    .queryBroadcastReceivers(Intent(Intent.ACTION_MEDIA_BUTTON), 96)
            // filter duplicate receivers
            val receivers = SimpleArrayMap<String, Boolean>(apps.size)

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
                mAppsList.put(appInfo, title)
            }
            mAppsList.sort()
            return mAppsList.entries
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
