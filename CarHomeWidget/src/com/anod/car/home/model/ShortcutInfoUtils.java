package com.anod.car.home.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.util.Log;

import com.anod.car.home.utils.FastBitmapDrawable;
import com.anod.car.home.utils.UtilitiesBitmap;

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
    
    private static ShortcutInfo infoFromShortcutIntent(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;

        final ShortcutInfo info = new ShortcutInfo();
        info.title = name;
        info.intent = intent;
        
        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = UtilitiesBitmap.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
            info.setCustomIcon(icon);
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
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
        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        Log.d("CarHomeWidget", "Component Name - " + componentName);
        
        final PackageManager manager = context.getPackageManager();
        
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

        return UtilitiesBitmap.createIconBitmap(
        		resolveInfo.activityInfo.loadIcon(manager), context
   	   );
    }
}
