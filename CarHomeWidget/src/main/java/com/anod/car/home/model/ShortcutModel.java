package com.anod.car.home.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;

import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.SoftReferenceThreadLocal;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ShortcutModel {

    final ContentResolver mContentResolver;
    private final PackageManager mPackageManager;
    private final Context mContext;
    private final int mIconBitmapSize;

    private ArrayList<SoftReference<Bitmap>> mUnusedBitmaps;

    private final static Object sLock = new Object();

    private SoftReferenceThreadLocal<Canvas> mCachedIconCanvas = new SoftReferenceThreadLocal<Canvas>() {
        @Override
        protected Canvas initialValue() {
            return new Canvas();
        }
    };

    private SoftReferenceThreadLocal<BitmapFactory.Options> mCachedBitmapFactoryOptions = new SoftReferenceThreadLocal<BitmapFactory.Options>() {
        @Override
        protected BitmapFactory.Options initialValue() {
            return new BitmapFactory.Options();
        }
    };

    public ShortcutModel(Context context) {
        mContentResolver = context.getContentResolver();
        mPackageManager = context.getPackageManager();
        mContext = context;
        mIconBitmapSize = UtilitiesBitmap.getIconMaxSize(context);
        mUnusedBitmaps = new ArrayList<>();
    }

    public ShortcutIcon loadShortcutIcon(Uri shortcutUri)
    {
        final Cursor c = mContentResolver.query(shortcutUri, null, null, null, null);
        if (c == null) {
            return null;
        }

        if (Utils.isLowMemoryDevice()) {
            mUnusedBitmaps.clear();
        }

        Bitmap unusedBitmap = null;
        synchronized(sLock) {
            // not in cache; we need to load it from the db
            while ((unusedBitmap == null || !unusedBitmap.isMutable() ||
                    unusedBitmap.getWidth() != mIconBitmapSize ||
                    unusedBitmap.getHeight() != mIconBitmapSize)
                    && mUnusedBitmaps.size() > 0) {
                unusedBitmap = mUnusedBitmaps.remove(0).get();
            }
            if (unusedBitmap != null) {
                final Canvas canvas = mCachedIconCanvas.get();
                canvas.setBitmap(unusedBitmap);
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.setBitmap(null);
            }

            if (unusedBitmap == null) {
                Bitmap.Config config = Utils.isLowMemoryDevice() ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
                unusedBitmap = Bitmap.createBitmap(mIconBitmapSize, mIconBitmapSize, config);
            }
        }

        ShortcutIcon shortcutIcon = null;
        try {
            c.moveToFirst();

            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int isCustomIconIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.IS_CUSTOM_ICON);

            final long id = c.getLong(idIndex);
            final int itemType = c.getInt(itemTypeIndex);

            Bitmap icon = null;
            if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                icon = getIconFromCursor(c, iconIndex, unusedBitmap);
                if (c.getInt(isCustomIconIndex) == 1) {
                    shortcutIcon = ShortcutIcon.forCustomIcon(id, icon);
                } else {
                    shortcutIcon = ShortcutIcon.forActivity(id, icon);
                }
            } else {
                int iconType = c.getInt(iconTypeIndex);
                if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
                    String packageName = c.getString(iconPackageIndex);
                    String resourceName = c.getString(iconResourceIndex);
                    Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
                    iconResource.packageName = packageName;
                    iconResource.resourceName = resourceName;
                    // the resource
                    try {
                        Resources resources = mPackageManager
                                .getResourcesForApplication(packageName);
                        if (resources != null) {
                            final int resId = resources.getIdentifier(resourceName, null, null);
                            if (resId > 0) {
                                Drawable iconDrawable = ResourcesCompat.getDrawable(resources, resId, null);
                                icon = UtilitiesBitmap.createHiResIconBitmap(iconDrawable, mContext);
                            }
                        }
                    } catch (NameNotFoundException | NotFoundException e) {
                        // drop this. we have other places to look for icons
                        AppLog.d(e.getMessage());
                    }
                    // the db
                    if (icon == null) {
                        icon = getIconFromCursor(c, iconIndex, unusedBitmap);
                    }
                    shortcutIcon = ShortcutIcon.forIconResource(id, icon, iconResource);
                } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                    icon = getIconFromCursor(c, iconIndex, unusedBitmap);
                    if (icon != null) {
                        shortcutIcon = ShortcutIcon.forCustomIcon(id, icon);
                    }
                }
            }

            if (icon == null) {
                icon = UtilitiesBitmap.makeDefaultIcon(mPackageManager);
                shortcutIcon = ShortcutIcon.forFallbackIcon(id, icon);
            }
        } catch (Exception e) {
            AppLog.e(e);
        } finally {
            c.close();
        }

        return shortcutIcon;
    }

    public ShortcutIcon loadShortcutIcon(long shortcutId)
    {
        Uri shortcutUri = LauncherSettings.Favorites.getContentUri(mContext.getPackageName(), shortcutId);
        return loadShortcutIcon(shortcutUri);
    }

    public Shortcut loadShortcut(long shortcutId) {
        String selection = LauncherSettings.Favorites._ID + "=?";
        String[] selectionArgs = { String.valueOf(shortcutId) };

        final Cursor c = mContentResolver
                .query(LauncherSettings.Favorites.getContentUri(mContext.getPackageName()), null,
                        selection, selectionArgs, null);

        if (c == null) {
            return null;
        }

        Shortcut info;
        try {
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            c.moveToFirst();
            Intent intent;
            String intentDescription = c.getString(intentIndex);
            if (TextUtils.isEmpty(intentDescription)) {
                return null;
            }
            try {
                intent = Intent.parseUri(intentDescription, 0);
            } catch (URISyntaxException e) {
                c.close();
                return null;
            }

            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int isCustomIconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.IS_CUSTOM_ICON);

            final long id = c.getLong(idIndex);
            final String title = c.getString(titleIndex);
            final int itemType = c.getInt(itemTypeIndex);
            final boolean isCustomIcon = c.getInt(isCustomIconIndex) == 1;

            info = new Shortcut(id, itemType, title, isCustomIcon, intent);
        } finally {
            c.close();
        }

        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex, Bitmap unusedBitmap) {
        byte[] data = c.getBlob(iconIndex);
        BitmapFactory.Options opts;

        opts = mCachedBitmapFactoryOptions.get();
        opts.outWidth = mIconBitmapSize;
        opts.outHeight = mIconBitmapSize;
        opts.inSampleSize = 1;
//        opts.inMutable = true;
        if (UtilitiesBitmap.canUseForInBitmap(unusedBitmap, opts)) {
            opts.inBitmap = unusedBitmap;
        }
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        } catch (Exception e) {
            AppLog.e(e);
           // throw new RuntimeException(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Add an item to the database in a specified container. Sets the container,
     * screen, cellX and cellY fields of the item. Also assigns an ID to the
     * item.
     */
    public long addItemToDatabase(Context context,@NonNull  Shortcut item, @NonNull ShortcutIcon icon) {

        final ContentResolver cr = context.getContentResolver();
        final ContentValues values = createShortcutContentValues(item, icon);

        Uri result = cr
                .insert(LauncherSettings.Favorites.getContentUri(context.getPackageName()), values);

        if (result != null) {
            return Integer.parseInt(result.getPathSegments().get(1));
        }
        return Shortcut.NO_ID;
    }

    /**
     * Update an item to the database in a specified container.
     */
    public void updateItemInDatabase(Context context, @NonNull Shortcut item, @NonNull ShortcutIcon icon) {
        final ContentResolver cr = context.getContentResolver();

        final ContentValues values = createShortcutContentValues(item, icon);

        cr.update(LauncherSettings.Favorites.getContentUri(context.getPackageName(), item.id),
                values, null, null);
    }

    public static ContentValues createShortcutContentValues(@NonNull Shortcut item, @NonNull ShortcutIcon icon) {
        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType);

        String titleStr = item.title == null ? null : item.title.toString();
        values.put(LauncherSettings.Favorites.TITLE, titleStr);

        String uri = item.intent == null ? null : item.intent.toUri(0);
        values.put(LauncherSettings.Favorites.INTENT, uri);

        if (icon.isCustom) {
            values.put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_BITMAP);
            writeBitmap(values, icon.bitmap);
        } else {
            if (!icon.isFallback) {
                writeBitmap(values, icon.bitmap);
            }
            values.put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_RESOURCE);
            if (icon.resource != null) {
                values.put(LauncherSettings.Favorites.ICON_PACKAGE,
                        icon.resource.packageName);
                values.put(LauncherSettings.Favorites.ICON_RESOURCE,
                        icon.resource.resourceName);
            }
        }
        values.put(LauncherSettings.Favorites.IS_CUSTOM_ICON, icon.isCustom ? 1 : 0);
        return values;
    }

    private static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = UtilitiesBitmap.flattenBitmap(bitmap);
            values.put(LauncherSettings.Favorites.ICON, data);
        }
    }

    /**
     * Removes the specified item from the database
     */
    public void deleteItemFromDatabase(long shortcutId) {
        final Uri uriToDelete = LauncherSettings.Favorites
                .getContentUri(mContext.getPackageName(), shortcutId);
        new Runnable() {
            public void run() {
                mContentResolver.delete(uriToDelete, null, null);
            }
        };
    }

}