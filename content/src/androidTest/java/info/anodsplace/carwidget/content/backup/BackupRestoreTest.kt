package info.anodsplace.carwidget.content.backup

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.util.JsonReader
import android.util.JsonWriter
import android.util.SparseArray
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import info.anodsplace.carwidget.content.db.Database
import info.anodsplace.carwidget.content.db.Favorites
import info.anodsplace.carwidget.content.db.FolderItem
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutWithFolderItems
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.StringReader
import java.io.StringWriter

@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {

    @Test
    fun shortcuts_backup_and_restore_roundtrip() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "test-backup-restore.db"
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

        // Seed original shortcuts
        val component = ComponentName(context.packageName, "TestActivity")
        val bmp = Bitmap.createBitmap(1, 1, Config.ARGB_8888)

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

        // Write backup JSON (only shortcuts section relevant for this test)
        val stringWriter = StringWriter()
        val writer = JsonWriter(stringWriter)
        writer.beginObject()
        writer.name("settings").beginObject().name("first-time").value(true).endObject()
        writer.name("incar").beginObject().endObject() // empty incar section
        writer.name("shortcuts").beginArray()
        writer.writeShortcuts(originalMap, shortcutsDb, context)
        writer.endArray()
        writer.name("notificationShortcuts").beginArray().endArray()
        writer.endObject()
        writer.close()

        val json = stringWriter.toString()

        // Read back the shortcuts list similar to BackupManager.doRestoreWidget path
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

        // Restore into new target id
        shortcutsDb.restoreTarget(RESTORE_TARGET, readShortcuts)
        val restoredMap = shortcutsDb.loadTarget(RESTORE_TARGET)

        assertEquals("Restored shortcuts count mismatch", originalMap.size, restoredMap.size)
        // Compare titles by position
        for (i in 0 until original.size) {
            assertEquals(original[i].title, restoredMap[i]?.title)
        }
    }

    companion object {
        private const val ORIGINAL_TARGET = 5
        private const val RESTORE_TARGET = 15
    }
}

