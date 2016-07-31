package com.anod.car.home.prefs.backup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.SparseArray;

import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.utils.ApiCompat;
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

    public SparseArray<ShortcutInfo> readList(JsonReader reader) throws IOException {
        SparseArray<ShortcutInfo> shortcuts = new SparseArray<>();
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

            Shortcut shortcut = readShortcut(reader, unusedBitmap);
            shortcuts.put(shortcut.pos, shortcut.info);
        }

        reader.endArray();
        return shortcuts;
    }

    private Shortcut readShortcut(JsonReader reader, Bitmap unusedBitmap) throws IOException {
        reader.beginObject();
        ShortcutInfo info = new ShortcutInfo();

        int pos = -1;
        int iconType = 0;
        byte[] iconData = null;
        String iconPackageName = "";
        String iconResourceName = "";
        boolean isCustomIcon = false;

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("pos")) {
                pos = reader.nextInt();
            } else if (name.equals(LauncherSettings.Favorites.ITEM_TYPE)) {
                info.itemType = reader.nextInt();
            } else if (name.equals(LauncherSettings.Favorites.TITLE)) {
                info.title = reader.nextString();
            } else if (name.equals(LauncherSettings.Favorites.INTENT)) {
                String intentDescription = reader.nextString();
                if (!TextUtils.isEmpty(intentDescription)) {
                    try {
                        info.intent = Intent.parseUri(intentDescription, 0);
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

        Bitmap icon = null;
        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            icon = decodeIcon(iconData, unusedBitmap);
            info.setActivityIcon(icon);
            info.setCustomIcon(isCustomIcon);
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
                        final int id = resources.getIdentifier(iconResourceName, null, null);
                        if (id > 0) {
                            icon = UtilitiesBitmap
                                    .createHiResIconBitmap(ApiCompat.getDrawable(resources, id), mContext);
                        }
                    }
                } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                    // drop this. we have other places to look for icons
                    AppLog.d(e.getMessage());
                }
                // the db
                if (icon == null) {
                    icon = decodeIcon(iconData, unusedBitmap);
                }
                info.setIconResource(icon, iconResource);
            } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                icon = decodeIcon(iconData, unusedBitmap);
                if (icon != null) {
                    info.setCustomIcon(icon);
                }
            }
        }

        if (icon == null) {
            icon = UtilitiesBitmap.makeDefaultIcon(mContext.getPackageManager());
            info.setFallbackIcon(icon);
        }

        reader.endObject();
        return new Shortcut(info, pos);
    }

    private static class Shortcut
    {
        public Shortcut(ShortcutInfo info, int pos) {
            this.info = info;
            this.pos = pos;
        }

        ShortcutInfo info;
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
