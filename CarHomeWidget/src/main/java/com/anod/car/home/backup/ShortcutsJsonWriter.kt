package com.anod.car.home.backup

import android.util.JsonWriter
import android.util.SparseArray

import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.ShortcutIcon
import com.anod.car.home.model.ShortcutsDatabase
import com.anod.car.home.model.Shortcuts

import java.io.IOException

/**
 * @author algavris
 * @date 08/04/2016.
 */
class ShortcutsJsonWriter {

    @Throws(IOException::class)
    fun writeList(shortcutsWriter: JsonWriter, shortcuts: SparseArray<Shortcut?>, model: Shortcuts) {
        for (idx in 0 until shortcuts.size()) {
            val pos = shortcuts.keyAt(idx)
            val info = shortcuts.get(pos) ?: continue
            shortcutsWriter.beginObject()
            val icon = model.iconLoader.load(info)
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

