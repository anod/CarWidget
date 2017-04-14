package com.anod.car.home.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author algavris
 * @date 14/04/2017.
 */

public class DrawableUri {
    private final Context mContext;

    public static class OpenResourceIdResult {
        public final Resources r;
        public final int id;

        public OpenResourceIdResult(Resources r, int id) {
            this.r = r;
            this.id = id;
        }
    }

    public DrawableUri(Context context) {
        mContext = context;
    }

    /**
     * Source android.widget.ImageView
     */
    public Drawable resolve(Uri uri) {
        Drawable d = null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            d = getDrawableByUri(uri);
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                int maxIconSize = UtilitiesBitmap.getIconMaxSize(mContext);
                Bitmap bmp = decodeSampledBitmapFromStream(uri, maxIconSize, maxIconSize);
                DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
                bmp.setDensity(dm.densityDpi);
                d = new BitmapDrawable(mContext.getResources(), bmp);
            } catch (Exception e) {
                Log.w("ShortcutEditActivity", "Unable to open content: " + uri, e);
            }

        } else {
            d = Drawable.createFromPath(uri.toString());
        }

        return d;
    }

    private Drawable getDrawableByUri(Uri uri) {
        Drawable d = null;
        try {
            // Load drawable through Resources, to get the source density information
            OpenResourceIdResult r = getResourceId(uri);
            d = ResourcesCompat.getDrawableForDensity(r.r, r.id, UtilitiesBitmap.getTargetDensity(mContext), null);
        } catch (Exception e) {
            Log.w("ShortcutEditActivity", "Unable to open content: " + uri, e);
        }
        return d;
    }

    public Bitmap decodeSampledBitmapFromStream(Uri uri, int reqWidth, int reqHeight)
            throws FileNotFoundException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        InputStream is = mContext.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(is, null, options);
        closeStream(is);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        is = mContext.getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
        closeStream(is);

        return bmp;
    }

    /**
     * From android.content.ContentResolver
     */
    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = mContext.getPackageManager().getResourcesForApplication(authority);
            } catch (PackageManager.NameNotFoundException ex) {
                throw new FileNotFoundException("No package found for authority: " + uri);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: " + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        return new OpenResourceIdResult(r, id);
    }

    static void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
