package com.anod.car.home.model;

import android.content.Intent;
import android.graphics.Bitmap;

/**
 * @author algavris
 * @date 22/08/2016.
 */

public class ShortcutIcon {

    public final long id;
    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    public final boolean isCustom;

    /**
     * Indicates whether we're using the default fallback icon instead of something from the
     * app.
     */
    final boolean isFallback;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    final Intent.ShortcutIconResource resource;

    /**
     * The application icon.
     */
    public final Bitmap bitmap;

    public ShortcutIcon(long id, boolean customIcon, boolean usingFallbackIcon, Intent.ShortcutIconResource iconResource, Bitmap bitmap) {
        this.id = id;
        this.isCustom = customIcon;
        this.isFallback = usingFallbackIcon;
        this.resource = iconResource;
        this.bitmap = bitmap;
    }

    public static ShortcutIcon forActivity(long id, Bitmap icon) {
        return new ShortcutIcon(id, false, false, null, icon);
    }

    public static ShortcutIcon forFallbackIcon(long id, Bitmap icon) {
        return new ShortcutIcon(id, false, true, null, icon);
    }

    public static ShortcutIcon forCustomIcon(long id, Bitmap icon) {
        return new ShortcutIcon(id, true, false, null, icon);
    }

    public static ShortcutIcon  forIconResource(long id, Bitmap icon, Intent.ShortcutIconResource res) {
        return new ShortcutIcon(id, false, false, res, icon);
    }

}
