package com.anod.car.home.appscache

import android.content.Context
import android.content.Intent
import com.anod.car.home.app.AppsListLoader
import com.anod.car.home.model.AppsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * @date 2014-09-02
 */
class AppsCacheLoader(context: Context, private val queryIntent: Intent) : AppsListLoader {

    private val packageManager = context.packageManager
    private val selfPackage = context.packageName

    override suspend fun loadAppsList(): List<AppsList.Entry> = withContext(Dispatchers.Default) {
        val list = mutableListOf<AppsList.Entry>()

        val apps = packageManager.queryIntentActivities(queryIntent, 0)
        for (appInfo in apps) {
            if (!appInfo.activityInfo.packageName.startsWith(selfPackage)) {
                val title = appInfo.activityInfo.loadLabel(packageManager).toString()
                list.add(AppsList.Entry(appInfo, title))
            }
        }

        list.sortBy { it.title }
        return@withContext list
    }
}
