package com.anod.car.home.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * @author alex
 * @date 2015-07-04
 */
public class BitmapLruCache extends LruCache<String, Bitmap> {


    public BitmapLruCache(Context context) {
        super(Utils.INSTANCE.calculateMemoryCacheSize(context));
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.

        return bitmap.getByteCount() / 1024;
    }
}
