package com.anod.car.home.model;

import android.content.ComponentName;
import android.content.Intent;

/**
 * @author algavris
 * @date 22/08/2016.
 */

public class Shortcut {

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    final public long id;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     */
    final public int itemType;

    /**
     * The application name.
     */
    final public CharSequence title;

    /**
     * The intent used to start the application.
     */
    final public Intent intent;

    final public boolean isCustomIcon;

    public Shortcut(long id, int itemType, CharSequence title, boolean isCustomIcon, Intent intent) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.intent = intent;
        this.isCustomIcon = isCustomIcon;
    }

    public Shortcut(long id, Shortcut item) {
        this(id, item.itemType, item.title, item.isCustomIcon, item.intent);
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION}.
     *
     * @param title
     * @param className   the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public static Shortcut forActivity(long id, CharSequence title, boolean isCustomIcon, ComponentName className, int launchFlags) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        return new Shortcut(id, LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, title, isCustomIcon, intent);
    }
}
