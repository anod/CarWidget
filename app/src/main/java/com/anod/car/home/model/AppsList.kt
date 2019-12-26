package com.anod.car.home.model


import android.content.ComponentName
import android.content.pm.ResolveInfo

import java.util.ArrayList

/**
 * Cache of application icons. Icons can be made from any thread.
 */
class AppsList {

    private val mCache = ArrayList<Entry>(INITIAL_ICON_CACHE_CAPACITY)

    val entries: ArrayList<Entry>
        get() = synchronized(mCache) {
            return mCache
        }

    class Entry(val componentName: ComponentName?, val iconRes: Int, var title: String) {

        constructor(info: ResolveInfo, title: String?):
            this(ComponentName(
                    info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name
            ), 0, title ?: info.activityInfo.name ?: "")
    }

    /**
     * Empty out the cache.
     */
    fun flush() {
        synchronized(mCache) {
            mCache.clear()
        }
    }

    fun replace(elements: List<Entry>) {
        synchronized(mCache) {
            mCache.clear()
            mCache.addAll(elements)
        }
    }

    companion object {
        private const val INITIAL_ICON_CACHE_CAPACITY = 50
    }


}