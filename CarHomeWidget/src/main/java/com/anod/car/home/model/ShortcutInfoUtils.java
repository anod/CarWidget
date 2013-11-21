package com.anod.car.home.model;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.anod.car.home.utils.FastBitmapDrawable;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

public class ShortcutInfoUtils {
    static final String TAG = "CarHomeWidget.ShortcutInfoUtils";
    public static ShortcutInfo createShortcut(Context context, Intent data, int cellId, boolean isAppShortcut) {
    	ShortcutInfo info = null;
    	if (isAppShortcut) {
    		info = infoFromApplicationIntent(context, data);
    	} else {
    		info = infoFromShortcutIntent(context, data);
    	}
        return info;
    }
    
    public static ShortcutInfo infoFromShortcutIntent(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;

        final ShortcutInfo info = new ShortcutInfo();
        info.title = name;
        info.intent = intent;
        
        if (bitmap instanceof Bitmap) {
            icon = UtilitiesBitmap.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
            info.setCustomIcon(icon);
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra instanceof ShortcutIconResource) {
                try {
                	ShortcutIconResource iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = UtilitiesBitmap.createIconBitmap(resources.getDrawable(id), context);
                    info.setIconResource(icon, iconResource);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        if (icon == null) {
        	final PackageManager packageManager = context.getPackageManager();
            icon = UtilitiesBitmap.makeDefaultIcon(packageManager);
            info.setFallbackIcon(icon);
        }

        return info;
    }
    
    
    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public static ShortcutInfo infoFromApplicationIntent(Context context, Intent intent) {
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        Log.d("CarHomeWidget", "Component Name - " + componentName);

        final PackageManager manager = context.getPackageManager();
        final ShortcutInfo info = new ShortcutInfo();
        Bitmap icon = null;

        info.setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            icon = getIcon(componentName, resolveInfo, manager, context);
            info.setActivityIcon(icon);
        }
        // the fallback icon
        if (icon == null) {
            icon = UtilitiesBitmap.makeDefaultIcon(manager);
            info.setFallbackIcon(icon);
        }

        // from the resource
        if (resolveInfo != null) {
            info.title = resolveInfo.activityInfo.loadLabel(manager);
        }

        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }
    
    private static Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo, PackageManager manager, Context context) {

        if (resolveInfo == null || component == null) {
            return null;
        }

		if (Utils.IS_JELLYBEAN_OR_GREATER) {
			return loadFullSizeIcon(manager, resolveInfo, context);
		}

		Drawable drawable = resolveInfo.activityInfo.loadIcon(manager);
        return UtilitiesBitmap.createIconBitmap(drawable, context);
    }

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	private static Bitmap loadFullSizeIcon(PackageManager manager, ResolveInfo resolveInfo, Context context) {
		try {
			Context otherAppCtxt = context.createPackageContext(resolveInfo.activityInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
			int icon = (resolveInfo.activityInfo.icon > 0) ? resolveInfo.activityInfo.icon : resolveInfo.activityInfo.applicationInfo.icon;
			if (icon == 0) {
				return null;
			}
			Drawable drawableAppIcon = otherAppCtxt.getResources().getDrawableForDensity(icon, DisplayMetrics.DENSITY_XXHIGH);
			if (drawableAppIcon instanceof BitmapDrawable) {
				return ((BitmapDrawable) drawableAppIcon).getBitmap();
			}
		} catch (PackageManager.NameNotFoundException e) {
			Utils.logd("NameNotFoundException: "+e.getMessage());
		}
		return null;
	}

	public static Drawable loadIcon(PackageManager pm, ApplicationInfo appInfo) {
// Get the application's resources
		Resources res = null;
		try {
			res = pm.getResourcesForApplication(appInfo);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}

// Get a copy of the configuration, and set it to the desired resolution
		Configuration config = res.getConfiguration();
		Configuration originalConfig = new Configuration(config);
		config.densityDpi = DisplayMetrics.DENSITY_XXHIGH;

// Update the configuration with the desired resolution
		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(config, dm);

// Grab the app icon
		Drawable appIcon = res.getDrawable(appInfo.icon);

// Set our configuration back to what it was
		res.updateConfiguration(originalConfig, dm);

		return appIcon;
	}
}
