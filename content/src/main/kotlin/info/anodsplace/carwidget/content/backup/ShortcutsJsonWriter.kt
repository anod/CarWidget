package info.anodsplace.carwidget.content.backup

import android.content.Context
import android.util.JsonWriter
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconConverter
import info.anodsplace.carwidget.content.db.ShortcutsDatabase

/**
 * @author algavris
 * @date 08/04/2016.
 */
class ShortcutsJsonWriter {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun writeList(shortcutsWriter: JsonWriter, shortcuts: Map<Int, Shortcut?>, db: ShortcutsDatabase, context: Context) {
        val iconConverter = ShortcutIconConverter.Default(context)

        for ((pos, value) in shortcuts) {
            val info = value ?: continue
            shortcutsWriter.beginObject()
            val dbShortcut = db.loadByShortcutId(info.id)
            val icon = iconConverter.convert(info.id, dbShortcut)
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

