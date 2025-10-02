package info.anodsplace.carwidget.content.db

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortcutsDatabaseMigrationTest {

    @Test
    fun migrateShortcutPosition_reordersAndAssignsTarget() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "test-shortcuts-migration.db"
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
        val bmp = Bitmap.createBitmap(1, 1, Config.ARGB_8888)

        val s1 = Shortcut.forActivity(0, 0, "One", false, component, 0)
        val s2 = Shortcut.forActivity(0, 1, "Two", false, component, 0)
        val s3 = Shortcut.forActivity(0, 2, "Three", false, component, 0)

        val id1 = shortcutsDb.addItem(0, 0, s1, ShortcutIcon.forActivity(0, bmp))
        val id2 = shortcutsDb.addItem(0, 1, s2, ShortcutIcon.forActivity(0, bmp))
        val id3 = shortcutsDb.addItem(0, 2, s3, ShortcutIcon.forActivity(0, bmp))

        // Reorder: new order will be id3, id1, id2
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "test-shortcuts-migration-unknown.db"
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
        val bmp = Bitmap.createBitmap(1, 1, Config.ARGB_8888)

        val s1 = Shortcut.forActivity(0, 0, "One", false, component, 0)
        val s2 = Shortcut.forActivity(0, 1, "Two", false, component, 0)

        val id1 = shortcutsDb.addItem(0, 0, s1, ShortcutIcon.forActivity(0, bmp))
        val id2 = shortcutsDb.addItem(0, 1, s2, ShortcutIcon.forActivity(0, bmp))

        val newOrder = arrayListOf(id2, Shortcut.ID_UNKNOWN, id1)
        val newTargetId = 11
        shortcutsDb.migrateShortcutPosition(newTargetId, newOrder)

        val result = shortcutsDb.loadTarget(newTargetId)
        // Unknown ID should not create a row; so only 2 rows with positions 0 and 2 or 0 and 2? Implementation increments index each loop, so positions 0 and 2 assigned.
        assertEquals(id2, result[0]?.id)
        // Position 1 should have no shortcut
        assertEquals(null, result[1]?.id)
        assertEquals(id1, result[2]?.id)
    }
}
