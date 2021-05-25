package info.anodsplace.carwidget.content.preferences

import android.content.ComponentName
import android.content.SharedPreferences

import androidx.collection.SimpleArrayMap

import info.anodsplace.applog.AppLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * @author algavris
 * @date 09/04/2016.
 */
open class ChangeableSharedPreferences(prefs: SharedPreferences) {

    private val _changes = MutableSharedFlow<Pair<String, Any?>>()
    val changes: SharedFlow<Pair<String, Any?>> = _changes
    private var pendingChanges = SimpleArrayMap<String, Any?>()
    var prefs: SharedPreferences
        protected set

    init {
        this.prefs = prefs
    }

    fun putChange(key: String, value: Any?) {
        pendingChanges.put(key, value)
        _changes.tryEmit(Pair(key, value))
    }

    fun apply() {
        if (pendingChanges.isEmpty) {
            return
        }
        val edit = prefs.edit()
        for (i in 0 until pendingChanges.size()) {
            val key = pendingChanges.keyAt(i)
            when (val value = pendingChanges.get(key)) {
                null -> edit.remove(key)
                is Boolean -> edit.putBoolean(key, value)
                is String -> edit.putString(key, value)
                is Int -> edit.putInt(key, value)
                is Long -> edit.putLong(key, value)
                is ComponentName -> edit.putString(key, value.flattenToString())
                else -> AppLog.e("Unknown value $value for key $key")
            }
        }
        edit.putBoolean("migrated", true)
        edit.apply()
        pendingChanges = SimpleArrayMap()
    }
}
