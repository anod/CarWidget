package com.anod.car.home.appscache

import com.anod.car.home.model.AppsList

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import com.anod.car.home.app.AppsListResultCallback

/**
 * @author alex
 * @date 2014-09-02
 */
class AppsCacheLoader(context: Context, private val queryIntent: Intent, private val callback: AppsListResultCallback) : AsyncTask<Void, Void, List<AppsList.Entry>>() {
    private val packageManager = context.packageManager
    private val selfPackage = context.packageName

    override fun doInBackground(vararg params: Void?): List<AppsList.Entry> {

        val list = mutableListOf<AppsList.Entry>()

        val apps = packageManager.queryIntentActivities(queryIntent, 0)
        for (appInfo in apps) {
            if (!appInfo.activityInfo.packageName.startsWith(selfPackage)) {
                val title = appInfo.activityInfo.loadLabel(packageManager).toString()
                list.add(AppsList.Entry(appInfo, title))
            }
        }

        list.sortBy { it.title }
        return list
    }

    override fun onPostExecute(result: List<AppsList.Entry>?) {
        super.onPostExecute(result)
        callback.onResult(result ?: emptyList())
    }
}
