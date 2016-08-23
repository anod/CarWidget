package com.anod.car.home.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.FastBitmapDrawable;
import com.anod.car.home.utils.UtilitiesBitmap;

public class ShortcutInfoUtils {

    static class ShortcutWithIcon {
        Shortcut info;
        ShortcutIcon icon;
    }

    static ShortcutWithIcon createShortcut(Context context, Intent data, boolean isAppShortcut) {
        ShortcutWithIcon info;
        if (isAppShortcut) {
            info = infoFromApplicationIntent(context, data);
        } else {
            info = infoFromShortcutIntent(context, data);
        }
        return info;
    }

    static ShortcutWithIcon infoFromShortcutIntent(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;

        ShortcutWithIcon result = new ShortcutWithIcon();

        if (bitmap instanceof Bitmap) {
            AppLog.d("Custom shortcut with Bitmap");
            icon = UtilitiesBitmap.createMaxSizeIcon(new FastBitmapDrawable((Bitmap) bitmap), context);
            result.icon = ShortcutIcon.forCustomIcon(0, icon);
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra instanceof ShortcutIconResource) {
                AppLog.d("Custom shortcut with Icon Resource");
                try {
                    ShortcutIconResource iconResource = (ShortcutIconResource) extra;
                    icon = getPackageIcon(context, iconResource);
                    result.icon = ShortcutIcon.forIconResource(Shortcut.NO_ID, icon, iconResource);
                } catch (Resources.NotFoundException | PackageManager.NameNotFoundException e) {
                    AppLog.e(e);
                }
            }
        }

        if (icon == null) {
            final PackageManager packageManager = context.getPackageManager();
            icon = UtilitiesBitmap.makeDefaultIcon(packageManager);
            result.icon = ShortcutIcon.forFallbackIcon(Shortcut.NO_ID, icon);
        }

        result.info = new Shortcut(0, LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT, name, result.icon.isCustom, intent);

        return result;
    }

    private static Bitmap getPackageIcon(Context context, ShortcutIconResource iconResource) throws PackageManager.NameNotFoundException {
        final PackageManager packageManager = context.getPackageManager();
        Resources resources = packageManager.getResourcesForApplication(iconResource.packageName);
        final int id = resources.getIdentifier(iconResource.resourceName, null, null);

        Drawable drawableIcon = loadDrawableForTargetDensity(id, resources, context);

        Bitmap icon;
        if (drawableIcon instanceof BitmapDrawable) {
            icon = ((BitmapDrawable) drawableIcon).getBitmap();
        } else if (drawableIcon != null) {
            icon = UtilitiesBitmap.createHiResIconBitmap(drawableIcon, context);
        } else {
            icon = null;
        }
        return icon;
    }


    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public static ShortcutWithIcon infoFromApplicationIntent(Context context, Intent intent) {
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        Log.d("CarHomeWidget", "Component Name - " + componentName);

        final PackageManager manager = context.getPackageManager();
        Bitmap icon = null;


        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
        // from the resource
        CharSequence title = null;
        if (resolveInfo != null) {
            title = resolveInfo.activityInfo.loadLabel(manager);
        }

        // fall back to the class name of the activity
        if (title == null) {
            title = componentName.getClassName();
        }

        ShortcutWithIcon result = new ShortcutWithIcon();

        if (resolveInfo != null) {
            icon = getIcon(componentName, resolveInfo, manager, context);
            result.icon = ShortcutIcon.forActivity(Shortcut.NO_ID, icon);
        }
        // the fallback icon
        if (icon == null) {
            icon = UtilitiesBitmap.makeDefaultIcon(manager);
            result.icon = ShortcutIcon.forFallbackIcon(Shortcut.NO_ID, icon);
        }

        result.info = Shortcut.forActivity(Shortcut.NO_ID, title, result.icon.isCustom, componentName, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        return result;
    }

    public static Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo,
            PackageManager manager, Context context) {

        if (resolveInfo == null || component == null) {
            return null;
        }

        Drawable drawable = loadHighResIcon(resolveInfo, context);
        if (drawable == null) {
            drawable = resolveInfo.activityInfo.loadIcon(manager);
        }

        if (drawable == null) {
            return null;
        }

        return UtilitiesBitmap.createHiResIconBitmap(drawable, context);
    }

    private static Drawable loadHighResIcon(ResolveInfo resolveInfo, Context context) {
        try {
            Context otherAppCtxt = context
                    .createPackageContext(resolveInfo.activityInfo.packageName,
                            Context.CONTEXT_IGNORE_SECURITY);
            int icon = (resolveInfo.activityInfo.icon > 0) ? resolveInfo.activityInfo.icon
                    : resolveInfo.activityInfo.applicationInfo.icon;
            if (icon == 0) {
                return null;
            }

            return loadDrawableForTargetDensity(icon, otherAppCtxt.getResources(), context);

        } catch (PackageManager.NameNotFoundException e) {
            AppLog.d("NameNotFoundException: " + e.getMessage());
        }
        return null;
    }

    private static Drawable loadDrawableForTargetDensity(int id, Resources resources,
            Context context) {
        Drawable drawableAppIcon = null;
        try {
            drawableAppIcon = ResourcesCompat.getDrawableForDensity(resources, id, UtilitiesBitmap.getTargetDensity(context), null);
        } catch (Resources.NotFoundException e) {
            AppLog.e(e);
        }

        if (drawableAppIcon == null) {
            //fallback to the default density
            try {
                drawableAppIcon = ResourcesCompat.getDrawable(resources, id, null);
            } catch (Resources.NotFoundException e) {
                AppLog.e(e);
                return null;
            }
        }
        return drawableAppIcon;
    }

}
