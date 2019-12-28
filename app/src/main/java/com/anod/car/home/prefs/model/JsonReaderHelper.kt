package com.anod.car.home.prefs.model

import android.util.JsonReader
import android.util.JsonToken
import androidx.collection.SimpleArrayMap
import info.anodsplace.framework.AppLog
import java.io.IOException

/**
 * @author algavris
 * @date 09/04/2016.
 */
object JsonReaderHelper {

    @Throws(IOException::class)
    fun readValues(reader: JsonReader, types: SimpleArrayMap<String, JsonToken>, prefs: ChangeableSharedPreferences): Int {
        var found = 0;
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (!types.containsKey(name)) {
                AppLog.e("No type for name: $name")
                reader.skipValue()
                continue
            }
            val type = types.get(name)
            val isNull = reader.peek() == JsonToken.NULL
            var skipped = false
            when (type) {
                JsonToken.BOOLEAN -> {
                    prefs.putChange(name, reader.nextBoolean())
                    found++
                }
                JsonToken.STRING -> {
                    prefs.putChange(name, if (isNull) null else reader.nextString())
                    found++
                }
                JsonToken.NUMBER -> {
                    prefs.putChange(name, if (isNull) null else reader.nextInt())
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
