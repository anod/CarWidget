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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.SoftReferenceThreadLocal;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ShortcutModel {

    private final ContentResolver mContentResolver;

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

    public ShortcutInfo loadShortcut(long shortcutId) {
        String selection = LauncherSettings.Favorites._ID + "=?";
        String[] selectionArgs = {String.valueOf(shortcutId)};

        final Cursor c = mContentResolver
                .query(LauncherSettings.Favorites.getContentUri(mContext.getPackageName()), null,
                        selection, selectionArgs, null);

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

        ShortcutInfo info;
        try {
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            c.moveToFirst();
            Intent intent;
            String intentDescription = c.getString(intentIndex);
            if (TextUtils.isEmpty(intentDescription)) {
                return null;
            }            try {
                intent = Intent.parseUri(intentDescription, 0);
            } catch (URISyntaxException e) {
                c.close();
                return null;
            }

            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int isCustomIconIndex = c
                    .getColumnIndexOrThrow(LauncherSettings.Favorites.IS_CUSTOM_ICON);

            info = new ShortcutInfo();
            info.id = c.getLong(idIndex);
            info.title = c.getString(titleIndex);
            info.itemType = c.getInt(itemTypeIndex);
            info.intent = intent;

            Bitmap icon = null;
            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                icon = getIconFromCursor(c, iconIndex, unusedBitmap);
                info.setActivityIcon(icon);
                info.setCustomIcon(c.getInt(isCustomIconIndex) == 1);
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
                            final int id = resources.getIdentifier(resourceName, null, null);
                            if (id > 0) {
                                icon = UtilitiesBitmap
                                        .createHiResIconBitmap(resources.getDrawable(id), mContext);
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
                    info.setIconResource(icon, iconResource);
                } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                    icon = getIconFromCursor(c, iconIndex, unusedBitmap);
                    if (icon != null) {
                        info.setCustomIcon(icon);
                    }
                }
            }

            if (icon == null) {
                icon = UtilitiesBitmap.makeDefaultIcon(mPackageManager);
                info.setFallbackIcon(icon);
            }
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
    public void addItemToDatabase(Context context, ShortcutInfo item, int cellId) {

        final ContentResolver cr = context.getContentResolver();
        final ContentValues values = createShortcutContentValues(item);

        Uri result = cr
                .insert(LauncherSettings.Favorites.getContentUri(context.getPackageName()), values);

        if (result != null) {
            item.id = Integer.parseInt(result.getPathSegments().get(1));
        }
    }

    /**
     * Update an item to the database in a specified container.
     */
    public void updateItemInDatabase(Context context, ShortcutInfo item) {
        final ContentResolver cr = context.getContentResolver();

        final ContentValues values = createShortcutContentValues(item);

        cr.update(LauncherSettings.Favorites.getContentUri(context.getPackageName(), item.id),
                values, null, null);
    }

    public static ContentValues createShortcutContentValues(@NonNull ShortcutInfo item) {
        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType);

        String titleStr = item.title == null ? null : item.title.toString();
        values.put(LauncherSettings.Favorites.TITLE, titleStr);

        String uri = item.intent == null ? null : item.intent.toUri(0);
        values.put(LauncherSettings.Favorites.INTENT, uri);

        if (item.isCustomIcon()) {
            values.put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_BITMAP);
            writeBitmap(values, item.getIcon());
        } else {
            if (!item.isUsingFallbackIcon()) {
                writeBitmap(values, item.getIcon());
            }
            values.put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_RESOURCE);
            if (item.getIconResource() != null) {
                values.put(LauncherSettings.Favorites.ICON_PACKAGE,
                        item.getIconResource().packageName);
                values.put(LauncherSettings.Favorites.ICON_RESOURCE,
                        item.getIconResource().resourceName);
            }
        }
        values.put(LauncherSettings.Favorites.IS_CUSTOM_ICON, item.isCustomIcon() ? 1 : 0);
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