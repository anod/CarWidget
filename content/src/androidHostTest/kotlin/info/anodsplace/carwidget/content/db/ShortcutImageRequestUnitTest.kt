package info.anodsplace.carwidget.content.db

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies [toImageRequest] wires the icon version into Coil's memory cache key so a changed
 * icon busts the cache and the shortcut preview refreshes. Generic request extras are ignored
 * when Coil computes the cache key, so the version must live in [ImageRequest.memoryCacheKeyExtras].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class ShortcutImageRequestUnitTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun shortcut(): Shortcut = Shortcut(
        id = 42L,
        position = 0,
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION,
        title = "Title",
        isCustomIcon = true,
        intent = Intent(Intent.ACTION_MAIN)
    )

    @Test
    fun toImageRequest_omitsVersionWhenIconVersionIsDefault() {
        val request = shortcut().toImageRequest(context, adaptiveIconStyle = "")
        assertFalse(request.memoryCacheKeyExtras.containsKey("version"))
    }

    @Test
    fun toImageRequest_writesIconVersionIntoMemoryCacheKeyExtras() {
        val request = shortcut().toImageRequest(context, adaptiveIconStyle = "", iconVersion = 123)
        assertEquals("123", request.memoryCacheKeyExtras["version"])
    }

    @Test
    fun toImageRequest_differentIconVersionsProduceDifferentCacheKeyExtras() {
        val first = shortcut().toImageRequest(context, adaptiveIconStyle = "", iconVersion = 1)
        val second = shortcut().toImageRequest(context, adaptiveIconStyle = "", iconVersion = 2)
        assertEquals("1", first.memoryCacheKeyExtras["version"])
        assertEquals("2", second.memoryCacheKeyExtras["version"])
    }
}
