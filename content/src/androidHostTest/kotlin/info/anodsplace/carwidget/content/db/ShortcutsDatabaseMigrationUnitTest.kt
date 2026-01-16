package info.anodsplace.carwidget.content.db

import android.content.ComponentName
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric migration of instrumentation ShortcutsDatabaseMigrationTest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class ShortcutsDatabaseMigrationUnitTest {

    @Test
    fun migrateShortcutPosition_reordersAndAssignsTarget() = runBlocking<Unit> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = null
        )
        val database = Database(
            driver = driver,
            favoritesAdapter = Favorites.Adapter(
                targetIdAdapter = IntColumnAdapter,
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter,
                positionAdapter = IntColumnAdapter
            ),
            FolderItemAdapter = FolderItem.Adapter(
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter,
            )
        )
        val shortcutsDb = ShortcutsDatabase(database)

        val component = ComponentName(context.packageName, "TestActivity")
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        val s1 = Shortcut.forActivity(0, 0, "One", false, component, 0)
        val s2 = Shortcut.forActivity(0, 1, "Two", false, component, 0)
        val s3 = Shortcut.forActivity(0, 2, "Three", false, component, 0)

        val id1 = shortcutsDb.addItem(0, 0, s1, ShortcutIcon.forActivity(0, bmp))
        val id2 = shortcutsDb.addItem(0, 1, s2, ShortcutIcon.forActivity(0, bmp))
        val id3 = shortcutsDb.addItem(0, 2, s3, ShortcutIcon.forActivity(0, bmp))

        val newOrder = arrayListOf(id3, id1, id2)
        val newTargetId = 10
        shortcutsDb.migrateShortcutPosition(newTargetId, newOrder)

        val result = shortcutsDb.loadTarget(newTargetId)
        assertEquals("Size should be 3", 3, result.size)
        assertEquals(id3, result[0]?.id)
        assertEquals(id1, result[1]?.id)
        assertEquals(id2, result[2]?.id)
    }

    @Test
    fun migrateShortcutPosition_skipsUnknownIds() = runBlocking<Unit> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = null
        )
        val database = Database(
            driver = driver,
            favoritesAdapter = Favorites.Adapter(
                targetIdAdapter = IntColumnAdapter,
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter,
                positionAdapter = IntColumnAdapter
            ),
            FolderItemAdapter = FolderItem.Adapter(
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter,
            )
        )
        val shortcutsDb = ShortcutsDatabase(database)

        val component = ComponentName(context.packageName, "TestActivity")
        val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        val s1 = Shortcut.forActivity(0, 0, "One", false, component, 0)
        val s2 = Shortcut.forActivity(0, 1, "Two", false, component, 0)

        val id1 = shortcutsDb.addItem(0, 0, s1, ShortcutIcon.forActivity(0, bmp))
        val id2 = shortcutsDb.addItem(0, 1, s2, ShortcutIcon.forActivity(0, bmp))

        val newOrder = arrayListOf(id2, Shortcut.ID_UNKNOWN, id1)
        val newTargetId = 11
        shortcutsDb.migrateShortcutPosition(newTargetId, newOrder)

        val result = shortcutsDb.loadTarget(newTargetId)
        assertEquals(id2, result[0]?.id)
        assertEquals(null, result[1]?.id)
        assertEquals(id1, result[2]?.id)
    }
}
