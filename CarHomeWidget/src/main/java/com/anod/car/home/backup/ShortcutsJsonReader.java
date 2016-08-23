package com.anod.car.home.backup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.SparseArray;

import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutIcon;
import info.anodsplace.android.log.AppLog;

import com.anod.car.home.utils.SoftReferenceThreadLocal;
import com.anod.car.home.utils.UtilitiesBitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * @author algavris
 * @date 08/04/2016.
 */
public class ShortcutsJsonReader {

    private final int mIconBitmapSize;
    private final ArrayList<SoftReference<Bitmap>> mUnusedBitmaps;
    private Context mContext;
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

    public ShortcutsJsonReader(Context context) {
        this.mContext = context;
        mIconBitmapSize = UtilitiesBitmap.getIconMaxSize(context);
        mUnusedBitmaps = new ArrayList<>();

    }

    public SparseArray<ShortcutWithIconAndPosition> readList(JsonReader reader) throws IOException {
        SparseArray<ShortcutWithIconAndPosition> shortcuts = new SparseArray<>();
        reader.beginArray();

        while (reader.hasNext())
        {
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
                    unusedBitmap = Bitmap.createBitmap(mIconBitmapSize, mIconBitmapSize, Bitmap.Config.ARGB_8888);
                }
            }

            ShortcutWithIconAndPosition shortcut = readShortcut(reader, unusedBitmap);
            shortcuts.put(shortcut.pos, shortcut);
        }

        reader.endArray();
        return shortcuts;
    }

    private ShortcutWithIconAndPosition readShortcut(JsonReader reader, Bitmap unusedBitmap) throws IOException {
        reader.beginObject();

        int pos = -1;
        int iconType = 0;
        int itemType = 0;
        byte[] iconData = null;
        String iconPackageName = "";
        String iconResourceName = "";
        CharSequence title = "";
        boolean isCustomIcon = false;
        Intent intent = null;

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pos")) {
                pos = reader.nextInt();
            } else if (name.equals(LauncherSettings.Favorites.ITEM_TYPE)) {
                itemType = reader.nextInt();
            } else if (name.equals(LauncherSettings.Favorites.TITLE)) {
                title = reader.nextString();
            } else if (name.equals(LauncherSettings.Favorites.INTENT)) {
                String intentDescription = reader.nextString();
                if (!TextUtils.isEmpty(intentDescription)) {
                    try {
                        intent = Intent.parseUri(intentDescription, 0);
                    } catch (URISyntaxException e) {
                        AppLog.e(e);
                    }
                }
            } else if (name.equals(LauncherSettings.Favorites.ICON_TYPE)) {
                iconType = reader.nextInt();
            } else if (name.equals(LauncherSettings.Favorites.ICON))
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                reader.beginArray();
                while (reader.hasNext()) {
                    baos.write(reader.nextInt());
                }
                reader.endArray();
                iconData = baos.toByteArray();
            } else if (name.equals(LauncherSettings.Favorites.ICON_PACKAGE)) {
                iconPackageName = reader.nextString();
            } else if (name.equals(LauncherSettings.Favorites.ICON_RESOURCE)) {
                iconResourceName = reader.nextString();
            } else if (name.equals(LauncherSettings.Favorites.IS_CUSTOM_ICON)) {
                isCustomIcon = reader.nextInt() == 1;
            }
        }

        Shortcut info = new Shortcut(Shortcut.NO_ID, itemType, title, isCustomIcon, intent);

        Bitmap bitmap = null;
        ShortcutIcon icon = null;
        if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            bitmap = decodeIcon(iconData, unusedBitmap);
            if (isCustomIcon) {
                icon = ShortcutIcon.forCustomIcon(Shortcut.NO_ID, bitmap);
            } else {
                icon = ShortcutIcon.forActivity(Shortcut.NO_ID, bitmap);
            }
        } else {
            if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
                Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
                iconResource.packageName = iconPackageName;
                iconResource.resourceName = iconResourceName;
                // the resource
                try {
                    Resources resources = mContext.getPackageManager()
                            .getResourcesForApplication(iconPackageName);
                    if (resources != null) {
                        final int resId = resources.getIdentifier(iconResourceName, null, null);
                        if (resId > 0) {
                            bitmap = UtilitiesBitmap.createHiResIconBitmap(ResourcesCompat.getDrawable(resources, resId, null), mContext);
                        }
                    }
                } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                    // drop this. we have other places to look for icons
                    AppLog.d(e.getMessage());
                }
                // the db
                if (bitmap == null) {
                    bitmap = decodeIcon(iconData, unusedBitmap);
                }
                if (bitmap != null) {
                    icon = ShortcutIcon.forIconResource(Shortcut.NO_ID, bitmap, iconResource);
                }
            } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                bitmap = decodeIcon(iconData, unusedBitmap);
                if (bitmap != null) {
                    icon = ShortcutIcon.forCustomIcon(Shortcut.NO_ID, bitmap);
                }
            }
        }

        if (bitmap == null) {
            bitmap = UtilitiesBitmap.makeDefaultIcon(mContext.getPackageManager());
            icon = ShortcutIcon.forFallbackIcon(Shortcut.NO_ID, bitmap);
        }

        reader.endObject();
        return new ShortcutWithIconAndPosition(info, icon, pos);
    }

    static class ShortcutWithIconAndPosition
    {
        public ShortcutWithIconAndPosition(Shortcut info, ShortcutIcon icon, int pos) {
            this.info = info;
            this.icon = icon;
            this.pos = pos;
        }

        Shortcut info;
        ShortcutIcon icon;
        int pos;
    }

    private Bitmap decodeIcon(byte[] data, Bitmap unusedBitmap) {
        if (data == null || data.length == 0)
        {
            return null;
        }
        BitmapFactory.Options opts = mCachedBitmapFactoryOptions.get();
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
}
