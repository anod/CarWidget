package info.anodsplace.carwidget.content.backup

import android.content.Context
import android.util.JsonWriter
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * @author algavris
 * @date 08/04/2016.
 */
class ShortcutsJsonWriter {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun writeList(shortcutsWriter: JsonWriter, shortcuts: Map<Int, Shortcut?>, db: ShortcutsDatabase, context: Context) {
        for ((pos, value) in shortcuts) {
            val info = value ?: continue
            shortcutsWriter.beginObject()
            val icon = ShortcutsDatabase.loadIconFromDatabase(info.id, context, db)
            val values = ShortcutsDatabase.createShortcutContentValues(info, icon)
            shortcutsWriter.name("pos").value(pos.toLong())

            for (key in values.keySet()) {
                val current = values.get(key)
                if (current != null) {
                    when (current) {
                        is String -> shortcutsWriter.name(key).value(current)
                        is Int -> shortcutsWriter.name(key).value(current)
                        is Boolean -> shortcutsWriter.name(key).value(current)
                        is ByteArray -> {
                            shortcutsWriter.name(key).beginArray()
                            for (i in current.indices) {
                                shortcutsWriter.value(current[i].toLong())
                            }
                            shortcutsWriter.endArray()
                        }
                        else -> throw RuntimeException("Not implemented: $current")
                    }
                }
            }
            shortcutsWriter.endObject()
        }
    }
}

