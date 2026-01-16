package info.anodsplace.carwidget.content.backup

import android.content.ContentValues
import android.content.Intent
import android.util.JsonReader
import android.util.JsonWriter
import info.anodsplace.applog.AppLog
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URISyntaxException

/**
 * Lightweight DSL helpers to simplify working with [JsonReader].
 */
internal inline fun JsonReader.readObject(block: () -> Unit) {
    beginObject()
    while (hasNext()) {
        block()
    }
    endObject()
}

internal inline fun JsonReader.readArray(block: () -> Unit) {
    beginArray()
    while (hasNext()) {
        block()
    }
    endArray()
}

internal inline fun JsonReader.forEachName(block: (String) -> Unit) {
    beginObject()
    while (hasNext()) {
        val name = nextName()
        block(name)
    }
    endObject()
}

internal fun JsonReader.readIntent(): Intent? {
    val intentDescription = nextString()
    if (intentDescription.isNotEmpty()) {
        try {
            return Intent.parseUri(intentDescription, 0)
        } catch (e: URISyntaxException) {
            AppLog.e(e)
        }
    }
    return null
}

/** Read current value as an int[] stored as bytes array (writer serialized raw bytes as ints). */
@Throws(IOException::class)
internal fun JsonReader.readIntArrayAsBytes(): ByteArray {
    val baos = ByteArrayOutputStream()
    beginArray()
    while (hasNext()) {
        baos.write(nextInt())
    }
    endArray()
    return baos.toByteArray()
}

/** JsonWriter helpers **/
internal inline fun JsonWriter.obj(block: JsonWriter.() -> Unit) {
    beginObject(); this.block(); endObject()
}

internal inline fun JsonWriter.array(name: String, block: JsonWriter.() -> Unit) {
    name(name); beginArray(); this.block(); endArray()
}

internal fun JsonWriter.field(name: String, value: Int) { name(name); value(value) }
internal fun JsonWriter.field(name: String, value: Long) { name(name); value(value) }
internal fun JsonWriter.field(name: String, value: Boolean) { name(name); value(value) }
internal fun JsonWriter.field(name: String, value: String) { name(name); value(value) }

internal fun JsonWriter.contentValues(values: ContentValues) {
    // Iterate stable order for deterministic backups
    val keys = values.keySet().toMutableList().sorted()
    for (k in keys) {
        val v = values.get(k)
        if (v == null) continue
        when (v) {
            is String -> field(k, v)
            is Int -> field(k, v)
            is Long -> field(k, v)
            is Boolean -> field(k, v)
            is ByteArray -> {
                // Encode as array of unsigned bytes (ints 0..255) to match reader expectation
                name(k)
                beginArray()
                v.forEach { writeInt -> value(writeInt.toInt() and 0xFF) }
                endArray()
            }
            else -> { /* ignore unsupported types */ }
        }
    }
}
