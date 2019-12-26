package com.anod.car.home.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.anod.car.home.utils.UtilitiesBitmap
import info.anodsplace.framework.AppLog

object ShortcutInfoUtils {

    class ShortcutWithIcon {
        var info: Shortcut? = null
        var icon: ShortcutIcon? = null
    }

    internal fun createShortcut(context: Context, data: Intent, isAppShortcut: Boolean): ShortcutWithIcon {
        return if (isAppShortcut) {
            infoFromApplicationIntent(context, data)
        } else {
            infoFromShortcutIntent(context, data)
        }
    }

    private fun infoFromShortcutIntent(context: Context, data: Intent): ShortcutWithIcon {
        val intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT) ?: Intent()
        val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: ""
        val bitmap = data.getParcelableExtra<Parcelable>(Intent.EXTRA_SHORTCUT_ICON)

        var icon: Bitmap? = null

        val result = ShortcutWithIcon()

        if (bitmap is Bitmap) {
            AppLog.d("Custom shortcut with Bitmap")
            icon = UtilitiesBitmap.createMaxSizeIcon(BitmapDrawable(context.resources, bitmap), context)
            result.icon = ShortcutIcon.forCustomIcon(0, icon)
        } else {
            val extra = data.getParcelableExtra<Parcelable>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            if (extra is ShortcutIconResource) {
                AppLog.d("Custom shortcut with Icon Resource")
                try {
                    icon = getPackageIcon(context, extra)
                    result.icon = ShortcutIcon.forIconResource(Shortcut.idUnknown, icon!!, extra)
                } catch (e: Resources.NotFoundException) {
                    AppLog.e(e)
                } catch (e: PackageManager.NameNotFoundException) {
                    AppLog.e(e)
                }

            }
        }

        if (icon == null) {
            val packageManager = context.packageManager
            icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            result.icon = ShortcutIcon.forFallbackIcon(Shortcut.idUnknown, icon)
        }

        result.info = Shortcut(0, LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT, name, result.icon!!.isCustom, intent)

        return result
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getPackageIcon(context: Context, iconResource: ShortcutIconResource): Bitmap? {
        val packageManager = context.packageManager
        val resources = packageManager.getResourcesForApplication(iconResource.packageName)
        val id = resources.getIdentifier(iconResource.resourceName, null, null)

        val drawableIcon = loadDrawableForTargetDensity(id, resources, context)

        val icon: Bitmap?
        when {
            drawableIcon is BitmapDrawable -> icon = drawableIcon.bitmap
            drawableIcon != null -> icon = UtilitiesBitmap.createHiResIconBitmap(drawableIcon, context)
            else -> icon = null
        }
        return icon
    }


    /**
     * Make an Shortcut object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    fun infoFromApplicationIntent(context: Context, intent: Intent): ShortcutWithIcon {
        val componentName = intent.component ?: return ShortcutWithIcon()
        Log.d("CarHomeWidget", "Component Name - $componentName")

        val manager = context.packageManager
        var icon: Bitmap? = null


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

        val result = ShortcutWithIcon()

        if (resolveInfo != null) {
            icon = getIcon(componentName, resolveInfo, manager, context)
            result.icon = ShortcutIcon.forActivity(Shortcut.idUnknown, icon!!)
        }
        // the fallback icon
        if (icon == null) {
            icon = UtilitiesBitmap.makeDefaultIcon(manager)
            result.icon = ShortcutIcon.forFallbackIcon(Shortcut.idUnknown, icon)
        }

        result.info = Shortcut.forActivity(Shortcut.idUnknown, title, result.icon!!.isCustom, componentName, Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        return result
    }

    fun getIcon(component: ComponentName?, resolveInfo: ResolveInfo?,
                manager: PackageManager, context: Context): Bitmap? {

        if (resolveInfo == null || component == null) {
            return null
        }

        var drawable = loadHighResIcon(resolveInfo, context)
        if (drawable == null) {
            drawable = resolveInfo.activityInfo.loadIcon(manager)
        }

        return if (drawable == null) {
            null
        } else UtilitiesBitmap.createHiResIconBitmap(drawable, context)

    }

    private fun loadHighResIcon(resolveInfo: ResolveInfo, context: Context): Drawable? {
        try {
            val otherAppCtxt = context.createPackageContext(resolveInfo.activityInfo.packageName,
                            Context.CONTEXT_IGNORE_SECURITY)
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

    private fun loadDrawableForTargetDensity(id: Int, resources: Resources,
                                             context: Context): Drawable? {
        var drawableAppIcon: Drawable? = null
        try {
            drawableAppIcon = ResourcesCompat.getDrawableForDensity(resources, id, UtilitiesBitmap.getTargetDensity(context), null)
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
