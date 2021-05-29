package info.anodsplace.carwidget.chooser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.collection.SimpleArrayMap
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface AppsListLoader {
    fun load(): Flow<List<ChooserEntry>>
}

class AppsPackageLoader(context: Context, private val queryIntent: Intent) : AppsListLoader {

    private val packageManager = context.packageManager
    private val selfPackage = context.packageName

    override fun load(): Flow<List<ChooserEntry>> = flow {
        emit(loadAll())
    }

    private suspend fun loadAll(): List<ChooserEntry> = withContext(Dispatchers.Default) {
        val list = mutableListOf<ChooserEntry>()

        val apps = packageManager.queryIntentActivities(queryIntent, 0)
        for (appInfo in apps) {
            if (!appInfo.activityInfo.packageName.startsWith(selfPackage)) {
                val title = appInfo.activityInfo.loadLabel(packageManager).toString()
                list.add(ChooserEntry(appInfo, title))
            }
        }

        list.sortBy { it.title }
        return@withContext list
    }
}


class MediaListLoader(context: Context) : AppsListLoader {
    private val packageManager = context.packageManager

    override fun load(): Flow<List<ChooserEntry>> = flow {
        emit(loadAll())
    }

    private suspend fun loadAll(): List<ChooserEntry> = withContext(Dispatchers.Default) {
        val apps = packageManager
            .queryBroadcastReceivers(Intent(Intent.ACTION_MEDIA_BUTTON), PackageManager.GET_RESOLVED_FILTER)
        // filter duplicate receivers
        val receivers = SimpleArrayMap<String, Boolean>(apps.size)
        val list = mutableListOf<ChooserEntry>()
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
            list.add(ChooserEntry(appInfo, title))
        }
        list.sortBy { it.title }
        return@withContext list
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