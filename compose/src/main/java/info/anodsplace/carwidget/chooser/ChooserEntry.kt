package info.anodsplace.carwidget.chooser
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.graphics.AppIconFetcher
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent

class Header(val headerId: Int, title: String, val iconVector: ImageVector? = null, iconRes: Int = 0, intent: Intent? = null) :
    ChooserEntry(null, title, iconRes, intent = intent)

open class ChooserEntry(
    val componentName: ComponentName?,
    var title: String,
    @param:DrawableRes
    val iconRes: Int = 0,
    val icon: Drawable? = null,
    var intent: Intent? = null,
    var extras: Bundle? = null,
    // Android application category or fallback category
    val category: Int = ApplicationInfo.CATEGORY_UNDEFINED
) {

    constructor(info: ResolveInfo, title: String?):
            this(
                ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name), title ?: info.activityInfo.name ?: "",
                category = info.category()
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
        icon = resolveInfo.loadIcon(pm),
        category = resolveInfo.category()
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

// Lightweight fallback derivation; O(1) per app (small iteration over intent filter categories)
fun ResolveInfo.category(): Int {
    val manifestCategory = activityInfo.applicationInfo.category
    val filter = this.filter ?: return manifestCategory

    // Additional heuristic: any receiver/activity handling ACTION_MEDIA_BUTTON is considered AUDIO.
    // This lets us classify audio apps even if they don't declare CATEGORY_APP_MUSIC on an activity.
    try {
        if (filter.hasAction(Intent.ACTION_MEDIA_BUTTON)) {
            return ApplicationInfo.CATEGORY_AUDIO
        }
    } catch (_: Exception) {
        // Ignore – fall back to manifest or category iteration
    }

    val it = try { filter.categoriesIterator() } catch (_: Exception) { null } ?: return manifestCategory
    while (it.hasNext()) {
        when (val cat = it.next()) {
            Intent.CATEGORY_APP_MUSIC -> return ApplicationInfo.CATEGORY_AUDIO
            Intent.CATEGORY_APP_MAPS -> return ApplicationInfo.CATEGORY_MAPS
            Intent.CATEGORY_APP_BROWSER -> return ApplicationInfo.CATEGORY_PRODUCTIVITY
            Intent.CATEGORY_APP_EMAIL -> return ApplicationInfo.CATEGORY_SOCIAL
            Intent.CATEGORY_APP_GALLERY -> return ApplicationInfo.CATEGORY_IMAGE
            Intent.CATEGORY_APP_MESSAGING -> return ApplicationInfo.CATEGORY_SOCIAL
            // Ignore other categories
            else -> if (AppLog.isDebug) {
                // Only log rarely; not performance critical
                AppLog.v("Unhandled intent category $cat for ${activityInfo.packageName}")
            }
        }
    }
    return manifestCategory
}

fun ChooserEntry.iconUri(context: Context): Uri {
    return if (componentName == null ) {
        if (iconRes != 0) {
            context.iconUri(iconRes = iconRes)
        } else Uri.EMPTY
    } else Uri.fromParts(AppIconFetcher.SCHEME_APPLICATION_ICON, componentName.flattenToShortString(), null)
}

fun ChooserEntry.toShortcutIntent(isApp: Boolean): ShortcutIntent {
    return ShortcutIntent(
        data = getIntent(null),
        isApp = isApp
    )
}