package info.anodsplace.carwidget.content.backup

import android.content.ComponentName
import android.graphics.Bitmap
import android.util.JsonReader
import android.util.JsonWriter
import android.util.SparseArray
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import info.anodsplace.carwidget.content.db.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringReader
import java.io.StringWriter

/**
 * Robolectric migration of instrumentation BackupRestoreTest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class BackupRestoreUnitTest {

    @Test
    fun shortcuts_backup_and_restore_roundtrip() = runBlocking<Unit> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "backup-restore-unit.db"
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

        val original = listOf(
            Shortcut.forActivity(0, 0, "One", false, component, 0),
            Shortcut.forActivity(0, 1, "Two", false, component, 0),
            Shortcut.forActivity(0, 2, "Three", false, component, 0)
        )
        original.forEachIndexed { index, shortcut ->
            shortcutsDb.addItem(ORIGINAL_TARGET, index, shortcut, ShortcutIcon.forActivity(0, bmp))
        }

        val originalMap = shortcutsDb.loadTarget(ORIGINAL_TARGET)
        assertEquals(3, originalMap.size)

        val stringWriter = StringWriter()
        val writer = JsonWriter(stringWriter)
        writer.beginObject()
        writer.name("settings").beginObject().name("first-time").value(true).endObject()
        writer.name("incar").beginObject().endObject()
        writer.name("shortcuts").beginArray()
        writer.writeShortcuts(originalMap, shortcutsDb, context)
        writer.endArray()
        writer.name("notificationShortcuts").beginArray().endArray()
        writer.endObject()
        writer.close()

        val json = stringWriter.toString()

        val shortcutsJsonReader = ShortcutsJsonReader(context)
        val reader = JsonReader(StringReader(json))
        var readShortcuts = SparseArray<ShortcutWithFolderItems>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "shortcuts" -> {
                    readShortcuts = runBlocking { shortcutsJsonReader.readList(reader) }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        reader.close()

        shortcutsDb.restoreTarget(RESTORE_TARGET, readShortcuts)
        val restoredMap = shortcutsDb.loadTarget(RESTORE_TARGET)

        assertEquals("Restored shortcuts count mismatch", originalMap.size, restoredMap.size)
        for (i in original.indices) {
            assertEquals(original[i].title, restoredMap[i]?.title)
        }
    }

    companion object {
        private const val ORIGINAL_TARGET = 5
        private const val RESTORE_TARGET = 15
    }
}
