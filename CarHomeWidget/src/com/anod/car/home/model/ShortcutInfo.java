package com.anod.car.home.model;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;


/**
 * Represents a launchable icon on the widget
 */
public class ShortcutInfo  {

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     */
    public int itemType;

    /**
     * The application name.
     */
    public CharSequence title;

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    private boolean customIcon;

	/**
     * Indicates whether we're using the default fallback icon instead of something from the
     * app.
     */
    private boolean usingFallbackIcon;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    private Intent.ShortcutIconResource iconResource;

    /**
     * The application icon.
     */
    private Bitmap mIcon;
    
    ShortcutInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
    }

    public ShortcutInfo(ShortcutInfo info) {
        id = info.id;
        itemType = info.itemType;

        title = info.title.toString();
        intent = new Intent(info.intent);
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
        mIcon = info.mIcon; // TODO: should make a copy here.  maybe we don't need this ctor at all
        customIcon = info.customIcon;
    }

    public void setActivityIcon(Bitmap icon) {
    	customIcon = false;
    	iconResource = null;
    	usingFallbackIcon = false;
        mIcon = icon;
    }
    
    public void setFallbackIcon(Bitmap icon) {
        mIcon = icon;
        iconResource = null;
        usingFallbackIcon = true;
        customIcon = false;
    }

    public void setCustomIcon(Bitmap icon) {
    	customIcon = true;
    	iconResource = null;
    	usingFallbackIcon = false;
        mIcon = icon;
    }
    
    public void setIconResource(Bitmap icon,Intent.ShortcutIconResource res) {
    	mIcon = icon;
    	iconResource = res;
    	usingFallbackIcon = false;
    	customIcon = false;
    }
    
    public Bitmap getIcon() {
        return mIcon;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    }

    public boolean isCustomIcon() {
		return customIcon;
	}    

    public boolean isUsingFallbackIcon() {
		return usingFallbackIcon;
	}

	public Intent.ShortcutIconResource getIconResource() {
		return iconResource;
	}

	@Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + ")";
    }

}