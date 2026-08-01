// Copyright (c) CarWidget contributors. Licensed under the project license.
package info.anodsplace.carwidget.content

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the LruCache sizing fix: sizeOf() must never round down to 0 for sub-kilobyte
 * bitmaps (which previously let entries accumulate without counting against the cache),
 * and the cache budget must be a positive number of kilobytes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class BitmapLruCacheUnitTest {

    private fun cache() = BitmapLruCache(ApplicationProvider.getApplicationContext())

    private fun bitmap(width: Int, height: Int) =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    @Test
    fun cacheBudgetIsPositiveKilobytes() {
        assertTrue(cache().maxSize() > 0)
    }

    @Test
    fun subKilobyteBitmapCountsAsOneKilobyte() {
        val cache = cache()
        cache.put("tiny", bitmap(1, 1))
        assertEquals(1, cache.size())
    }

    @Test
    fun largerBitmapIsMeasuredInKilobytes() {
        val cache = cache()
        // 64 x 64 x 4 bytes = 16384 bytes = 16 KB
        cache.put("big", bitmap(64, 64))
        assertEquals(16, cache.size())
    }

    @Test
    fun storedBitmapCanBeRetrieved() {
        val cache = cache()
        val bmp = bitmap(2, 2)
        cache.put("k", bmp)
        assertSame(bmp, cache.get("k"))
    }
}
