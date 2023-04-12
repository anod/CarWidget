package info.anodsplace.carwidget.chooser

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.collection.SimpleArrayMap
import androidx.compose.ui.graphics.vector.ImageVector
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.BuildConfig
import info.anodsplace.carwidget.content.graphics.AppIconFetcher
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.framework.content.forLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class Header(val headerId: Int, title: String, val iconVector: ImageVector) :
    ChooserEntry(null, title)

open class ChooserEntry(
    val componentName: ComponentName?,
    var title: String,
    @DrawableRes val iconRes: Int = 0,
    val icon: Drawable? = null,
    var intent: Intent? = null,
    var extras: Bundle? = null
) {

    constructor(info: ResolveInfo, title: String?):
            this(
                ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name), title ?: info.activityInfo.name ?: ""
            )

    constructor(title: String, icon: Drawable?)
            : this(componentName = null, title = title, icon = icon)

    constructor(pm: PackageManager, resolveInfo: ResolveInfo)
            : this(
        componentName = ComponentName(
            resolveInfo.activityInfo.applicationInfo.packageName,
            resolveInfo.activityInfo.name
        ),
        title = resolveInfo.loadLabel(pm).toString(),
        icon = resolveInfo.loadIcon(pm)
    )

    /**
     * Build the [Intent] described by this item. If this item
     * can't create a valid [android.content.ComponentName], it
     * will return [Intent.ACTION_CREATE_SHORTCUT] filled with the
     * item label.
     */
    fun getIntent(baseIntent: Intent?): Intent {
        if (this.intent != null) {
            return this.intent!!
        }
        val intent = if (baseIntent != null) {
            Intent(baseIntent)
        } else {
            Intent(Intent.ACTION_MAIN)
        }
        if (componentName != null) {
            // Valid package and class, so fill details as normal intent
            intent.component = componentName
            if (extras != null) {
                intent.putExtras(extras!!)
            }
        } else {
            intent.action = Intent.ACTION_CREATE_SHORTCUT
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
        }
        return intent
    }
}

internal fun ChooserEntry.iconUri(context: Context): Uri {
    return if (componentName == null ) {
        if (iconRes != 0) {
            context.iconUri(iconRes = iconRes)
        } else Uri.EMPTY
    } else Uri.fromParts(AppIconFetcher.SCHEME_APPLICATION_ICON, componentName.flattenToShortString(), null)
}

interface ChooserLoader {
    fun load(): Flow<List<ChooserEntry>>
}

class StaticChooserLoader(private val list: List<ChooserEntry>) : ChooserLoader {
    override fun load(): Flow<List<ChooserEntry>> = flowOf(list)
}

class AllAppsIntentLoader(context: Context) : QueryIntentLoader(context, Intent().forLauncher())

open class QueryIntentLoader(context: Context, private val queryIntent: Intent) : ChooserLoader {

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

class MediaListLoader(context: Context) : ChooserLoader {
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

            val title = appInfo.activityInfo.applicationInfo.loadLabel(packageManager)
            if (BuildConfig.DEBUG) {
                AppLog.d(appInfo.activityInfo.packageName + "/"
                        + appInfo.activityInfo.applicationInfo.className)
            }
            receivers.put(pkg, true)
            list.add(ChooserEntry(appInfo, title.toString()))
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
            "com.sec.android.app.voicenote",
            "com.sec.android.app.vepreload",
            "com.sec.android.app.ve.vebgm"
        )
    }
}