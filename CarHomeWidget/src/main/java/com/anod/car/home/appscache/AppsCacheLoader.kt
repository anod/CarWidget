package com.anod.car.home.appscache

import com.anod.car.home.model.AppsList

import android.content.AsyncTaskLoader
import android.content.Context
import android.content.Intent

import java.util.ArrayList

/**
 * @author alex
 * @date 2014-09-02
 */
class AppsCacheLoader(context: Context, private val callback: Callback, private val appsList: AppsList) : AsyncTaskLoader<List<AppsList.Entry>>(context) {

    interface Callback {
        fun onIntentFilterInit(intent: Intent)
    }

    override fun loadInBackground(): ArrayList<AppsList.Entry> {
        loadAllAppsToCache()
        return appsList.entries
    }

    private fun loadAllAppsToCache() {
        appsList.flush()
        val mainIntent = Intent()
        callback.onIntentFilterInit(mainIntent)

        val packageManager = context.packageManager
        val apps = packageManager.queryIntentActivities(mainIntent, 0)
        val selfPackage = context.packageName
        for (appInfo in apps) {

            if (!appInfo.activityInfo.packageName.startsWith(selfPackage)) {
                val title = appInfo.activityInfo.loadLabel(packageManager).toString()
                appsList.put(appInfo, title)
            }
        }
        appsList.sort()
    }
}
