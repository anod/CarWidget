package info.anodsplace.carwidget.content.preferences

import android.content.ComponentName
import android.content.SharedPreferences

import androidx.collection.SimpleArrayMap

import info.anodsplace.applog.AppLog

/**
 * @author algavris
 * @date 09/04/2016.
 */
open class ChangeableSharedPreferences(prefs: SharedPreferences) {

    private var changes = SimpleArrayMap<String, Any?>()
    var prefs: SharedPreferences
        protected set

    init {
        this.prefs = prefs
    }

    fun putChange(key: String, value: Any?) {
        changes.put(key, value)
    }

    fun apply() {
        if (changes.isEmpty) {
            return
        }
        val edit = prefs.edit()
        for (i in 0 until changes.size()) {
            val key = changes.keyAt(i)
            when (val value = changes.get(key)) {
                null -> edit.remove(key)
                is Boolean -> edit.putBoolean(key, (value as Boolean?)!!)
                is String -> edit.putString(key, value as String?)
                is Int -> edit.putInt(key, (value as Int?)!!)
                is Long -> edit.putLong(key, (value as Long?)!!)
                is ComponentName -> edit.putString(key, value.flattenToString())
                else -> AppLog.e("Unknown value $value for key $key")
            }
        }
        edit.putBoolean("migrated", true)
        edit.apply()
        changes = SimpleArrayMap()
    }
}
