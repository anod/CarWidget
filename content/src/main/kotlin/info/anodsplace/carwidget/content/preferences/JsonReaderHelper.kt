package info.anodsplace.carwidget.content.preferences

import android.util.JsonReader
import android.util.JsonToken
import androidx.collection.SimpleArrayMap
import info.anodsplace.applog.AppLog
import java.io.IOException

/**
 * @author algavris
 * @date 09/04/2016.
 */
object JsonReaderHelper {

    @Throws(IOException::class)
    fun readValues(reader: JsonReader, types: SimpleArrayMap<String, JsonToken>, prefs: ChangeableSharedPreferences, typeHandler: (name: String, reader: JsonReader) -> Boolean): Int {
        var found = 0
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (!types.containsKey(name)) {
                if (!typeHandler(name, reader)) {
                    AppLog.e("No type for name: $name")
                    reader.skipValue()
                }
                continue
            }
            val type = types.get(name)
            val isNull = reader.peek() == JsonToken.NULL
            var skipped = false
            when (type) {
                JsonToken.BOOLEAN -> {
                    prefs.queueChange(name, reader.nextBoolean())
                    found++
                }
                JsonToken.STRING -> {
                    prefs.queueChange(name, if (isNull) null else reader.nextString())
                    found++
                }
                JsonToken.NUMBER -> {
                    prefs.queueChange(name, if (isNull) null else reader.nextInt())
                    found++
                }
                else -> {
                    AppLog.e("Unknown type: $type for name: $name")
                    skipped = true
                    reader.skipValue()
                }
            }
            if (isNull && !skipped) {
                reader.nextNull()
            }
        }
        return found
    }
}
