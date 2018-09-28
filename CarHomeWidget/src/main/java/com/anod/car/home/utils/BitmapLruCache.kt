package com.anod.car.home.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache

/**
 * @author alex
 * @date 2015-07-04
 */
class BitmapLruCache(context: Context) : LruCache<String, Bitmap>(Utils.calculateMemoryCacheSize(context)) {

    override fun sizeOf(key: String, bitmap: Bitmap): Int {
        // The cache size will be measured in kilobytes rather than
        // number of items.

        return bitmap.byteCount / 1024
    }
}
