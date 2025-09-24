package info.anodsplace.carwidget.content.shortcuts

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.extentions.toBitmap
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap

sealed interface CreateShortcutResult {
    interface CreateShortcutResultSuccess {
        val info: Shortcut
        val icon: ShortcutIcon
    }

    data object None : CreateShortcutResult

    data class SuccessApp(override val info: Shortcut, override val icon: ShortcutIcon) : CreateShortcutResult, CreateShortcutResultSuccess
    data class SuccessCustom(override val info: Shortcut, override val icon: ShortcutIcon) : CreateShortcutResult, CreateShortcutResultSuccess
    data class SuccessAppShortcut(override val info: Shortcut, override val icon: ShortcutIcon) : CreateShortcutResult, CreateShortcutResultSuccess
    data object FailedApp : CreateShortcutResult
    data object FailedAppShortcut : CreateShortcutResult
}

object ShortcutInfoFactory {


    internal fun createShortcut(
        context: Context,
        position: Int,
        data: Intent,
        isAppShortcut: Boolean
    ): CreateShortcutResult {
        return if (isAppShortcut) {
            infoFromApplicationIntent(context, position, data)
        } else {
            infoFromShortcutIntent(context, position, data)
        }
    }

    private fun infoFromShortcutIntent(context: Context, position: Int, data: Intent): CreateShortcutResult {

        // Use fully supported EXTRA_SHORTCUT_INTENT when available
        if (!data.hasExtra(Intent.EXTRA_SHORTCUT_INTENT) && data.hasExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST)) {
            val item =
                data.getParcelableExtra<LauncherApps.PinItemRequest>(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
                    ?: return CreateShortcutResult.FailedAppShortcut
            return shortcutFromPinItemRequest(item, context, position)
        }

        val intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT) ?: Intent()
        val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: ""
        val extraIcon = data.getParcelableExtra<Parcelable>(Intent.EXTRA_SHORTCUT_ICON)
        var bitmap: Bitmap? = null
        var icon: ShortcutIcon? = null

        if (extraIcon is Bitmap) {
            AppLog.d("Custom shortcut with Bitmap")
            bitmap = UtilitiesBitmap.createMaxSizeIcon(
                extraIcon.toDrawable(context.resources),
                context
            )
            icon = ShortcutIcon.forCustomIcon(0, bitmap)
        } else {
            val extra = data.getParcelableExtra<Parcelable>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            if (extra is ShortcutIconResource) {
                AppLog.d("Custom shortcut with Icon Resource")
                try {
                    bitmap = getPackageIcon(context, extra)
                    icon = ShortcutIcon.forIconResource(Shortcut.ID_UNKNOWN, bitmap!!, extra)
                } catch (e: Resources.NotFoundException) {
                    AppLog.e(e)
                } catch (e: PackageManager.NameNotFoundException) {
                    AppLog.e(e)
                }

            }
        }

        if (bitmap == null) {
            val packageManager = context.packageManager
            bitmap = UtilitiesBitmap.makeDefaultIcon(packageManager)
            icon = ShortcutIcon.forFallbackIcon(Shortcut.ID_UNKNOWN, bitmap)
        }

        return CreateShortcutResult.SuccessCustom(
            info = Shortcut(
                id = 0,
                position = position,
                itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT,
                title = name,
                isCustomIcon = icon!!.isCustom,
                intent = intent
            ),
            icon = icon
        )
    }

    private fun shortcutFromPinItemRequest(
        pinItem: LauncherApps.PinItemRequest,
        context: Context,
        position: Int
    ): CreateShortcutResult {
        val shortcutInfo = pinItem.shortcutInfo
            ?: return CreateShortcutResult.FailedAppShortcut

        val launcherApps = context.getSystemService(LauncherApps::class.java)
        var bitmap: Bitmap? = null
        try {
            bitmap = launcherApps?.getShortcutIconDrawable(
                shortcutInfo,
                UtilitiesBitmap.getTargetDensity(context)
            ).toBitmap(context)
        } catch (e: SecurityException) {
            AppLog.e("Failed to get shortcut icon", e)
        } catch (e: IllegalStateException) {
            AppLog.e("Failed to get shortcut icon", e)
        }

        val idData = shortcutInfo.id.toUri()
        val intent = shortcutInfo.intent ?: Intent().also {
            it.component = shortcutInfo.activity
            if (ContentResolver.SCHEME_CONTENT == idData.scheme) {
                it.data = idData
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        val label = shortcutInfo.shortLabel ?: ""
        val resolveInfo = context.packageManager.resolveActivity(intent, 0)
        val icon = if (bitmap == null)
            resolveAppIcon(resolveInfo, shortcutInfo.activity, context)
        else
            ShortcutIcon.forCustomIcon(Shortcut.ID_UNKNOWN, bitmap)

        return CreateShortcutResult.SuccessAppShortcut(
            icon = icon,
            info = Shortcut(
                0,
                position,
                LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT,
                label,
                icon.isCustom,
                intent
            )
        )
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getPackageIcon(context: Context, iconResource: ShortcutIconResource): Bitmap? {
        val packageManager = context.packageManager
        val resources = packageManager.getResourcesForApplication(iconResource.packageName)
        val id = resources.getIdentifier(iconResource.resourceName, null, null)

        return loadDrawableForTargetDensity(id, resources, context).toBitmap(context)
    }


    /**
     * Make an Shortcut object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    fun infoFromApplicationIntent(context: Context, position: Int, intent: Intent): CreateShortcutResult {
        val componentName = intent.component
            ?: return CreateShortcutResult.FailedAppShortcut
        AppLog.d("Component Name - $componentName")

        val manager = context.packageManager
        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        val resolveInfo = manager.resolveActivity(intent, 0)
        // from the resource
        var title: CharSequence? = null
        if (resolveInfo != null) {
            title = resolveInfo.activityInfo.loadLabel(manager)
        }

        // fall back to the class name of the activity
        if (title == null) {
            title = componentName.className
        }

        val icon = resolveAppIcon(resolveInfo, componentName, context)
        return CreateShortcutResult.SuccessApp(
            icon = icon,
            info = Shortcut.forActivity(
                Shortcut.ID_UNKNOWN,
                position,
                title,
                icon.isCustom,
                componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
        )
    }

    fun resolveAppIcon(
        resolveInfo: ResolveInfo?,
        componentName: ComponentName?,
        context: Context
    ): ShortcutIcon {
        var bitmap: Bitmap?
        if (resolveInfo != null) {
            bitmap = getIcon(componentName, resolveInfo, context.packageManager, context)
            if (bitmap != null) {
                return ShortcutIcon.forActivity(Shortcut.ID_UNKNOWN, bitmap)
            }
        }
        // the fallback icon
        bitmap = UtilitiesBitmap.makeDefaultIcon(context.packageManager)
        return ShortcutIcon.forFallbackIcon(Shortcut.ID_UNKNOWN, bitmap)
    }

    private fun getIcon(
        component: ComponentName?, resolveInfo: ResolveInfo?,
        manager: PackageManager, context: Context
    ): Bitmap? {

        if (resolveInfo == null || component == null) {
            return null
        }

        var drawable = loadHighResIcon(resolveInfo, context)
        if (drawable == null) {
            drawable = resolveInfo.activityInfo.loadIcon(manager)
        }

        return drawable.toBitmap(context)
    }

    private fun loadHighResIcon(resolveInfo: ResolveInfo, context: Context): Drawable? {
        try {
            val otherAppCtxt = context.createPackageContext(
                resolveInfo.activityInfo.packageName,
                Context.CONTEXT_IGNORE_SECURITY
            )
            val icon = if (resolveInfo.activityInfo.icon > 0)
                resolveInfo.activityInfo.icon
            else
                resolveInfo.activityInfo.applicationInfo.icon
            return if (icon == 0) {
                null
            } else loadDrawableForTargetDensity(icon, otherAppCtxt.resources, context)

        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.i("NameNotFoundException: " + e.message)
        }

        return null
    }

    private fun loadDrawableForTargetDensity(
        id: Int, resources: Resources,
        context: Context
    ): Drawable? {
        var drawableAppIcon: Drawable? = null
        try {
            drawableAppIcon = ResourcesCompat.getDrawableForDensity(
                resources,
                id,
                UtilitiesBitmap.getTargetDensity(context),
                null
            )
        } catch (e: Resources.NotFoundException) {
            AppLog.e(e)
        }

        if (drawableAppIcon == null) {
            //fallback to the default density
            try {
                drawableAppIcon = ResourcesCompat.getDrawable(resources, id, null)
            } catch (e: Resources.NotFoundException) {
                AppLog.e(e)
                return null
            }

        }
        return drawableAppIcon
    }

}
