package info.anodsplace.carwidget.content.backup

import android.util.JsonWriter
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import java.io.IOException

/**
 * @author algavris
 * @date 08/04/2016.
 */
class ShortcutsJsonWriter {

    @Throws(IOException::class)
    fun writeList(shortcutsWriter: JsonWriter, shortcuts: Map<Int, Shortcut?>, model: Shortcuts) {
        for ((pos, value) in shortcuts) {
            val info = value ?: continue
            shortcutsWriter.beginObject()
            val icon = model.iconLoader.loadFromDatabase(info.id)
            val values = ShortcutsDatabase.createShortcutContentValues(info, icon)
            shortcutsWriter.name("pos").value(pos.toLong())

            for (key in values.keySet()) {
                val value = values.get(key)
                if (value != null) {
                    when (value) {
                        is String -> shortcutsWriter.name(key).value(value)
                        is Int -> shortcutsWriter.name(key).value(value)
                        is Boolean -> shortcutsWriter.name(key).value(value)
                        is ByteArray -> {
                            shortcutsWriter.name(key).beginArray()
                            for (i in value.indices) {
                                shortcutsWriter.value(value[i].toLong())
                            }
                            shortcutsWriter.endArray()
                        }
                        else -> throw RuntimeException("Not implemented: $value")
                    }
                }
            }
            shortcutsWriter.endObject()
        }
    }
}

