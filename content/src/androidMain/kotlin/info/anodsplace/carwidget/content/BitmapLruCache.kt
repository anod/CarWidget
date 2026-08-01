package info.anodsplace.carwidget.content

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.util.LruCache
import kotlin.math.max

/**
 * @author alex
 * @date 2015-07-04
 */
class BitmapLruCache(context: Context) : LruCache<String, Bitmap>(calculateMemoryCacheSize(context)) {

    override fun sizeOf(key: String, bitmap: Bitmap): Int {
        // The cache size will be measured in kilobytes rather than
        // number of items.

        return max(1, bitmap.byteCount / 1024)
    }

    companion object {
        private fun calculateMemoryCacheSize(context: Context): Int {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val largeHeap = context.applicationInfo?.flags?.and(ApplicationInfo.FLAG_LARGE_HEAP)
            val memoryClass = if (largeHeap != 0) am.largeMemoryClass else am.memoryClass
            // Target ~15% of the available heap, measured in kilobytes to match sizeOf().
            return memoryClass * 1024 / 7
        }
    }
}
