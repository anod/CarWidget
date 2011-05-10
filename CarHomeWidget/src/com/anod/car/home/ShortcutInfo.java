package com.anod.car.home;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Represents a launchable icon on the widget
 */
class ShortcutInfo  {

    static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    long id = NO_ID;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     */
    int itemType;

    /**
     * The application name.
     */
    CharSequence title;

    /**
     * The intent used to start the application.
     */
    Intent intent;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    boolean customIcon;

    /**
     * Indicates whether we're using the default fallback icon instead of something from the
     * app.
     */
    boolean usingFallbackIcon;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    Intent.ShortcutIconResource iconResource;

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

    public void setIcon(Bitmap b) {
        mIcon = b;
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
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    }


    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + ")";
    }

}