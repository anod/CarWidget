package info.anodsplace.carwidget.content.preferences

import android.content.ComponentName
import android.content.SharedPreferences

import androidx.collection.SimpleArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import info.anodsplace.applog.AppLog
import kotlinx.coroutines.flow.*

/**
 * @author algavris
 * @date 09/04/2016.
 */
open class ChangeableSharedPreferences(prefs: SharedPreferences) {

    private val _changes = MutableStateFlow<Pair<String, Any?>?>(null)
    val changes: Flow<Pair<String, Any?>> = _changes.filterNotNull()
    private var pendingChanges = SimpleArrayMap<String, Any?>()
    var prefs: SharedPreferences
        protected set

    init {
        this.prefs = prefs
    }

    fun queueChange(key: String, value: Any?) {
        pendingChanges.put(key, value)
        _changes.value = Pair(key, value)
    }

    fun applyChange(key: String, value: Any?) {
        val edit = prefs.edit()
        putChange(edit, key, value)
        edit.apply()
        _changes.value = Pair(key, value)
    }

    fun <T : Any?> observe(key: String): Flow<T> = changes.filter { it.first == key }.map { it.second as T }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun applyPending() {
        if (pendingChanges.isEmpty) {
            return
        }
        val edit = prefs.edit()
        for (i in 0 until pendingChanges.size()) {
            val key = pendingChanges.keyAt(i)
            putChange(edit, key, pendingChanges.get(key))
        }
        edit.putBoolean("migrated", true)
        edit.apply()
        pendingChanges = SimpleArrayMap()
    }

    private fun putChange(edit: SharedPreferences.Editor, key: String, value: Any?) {
        when (value) {
            null -> edit.remove(key)
            is Boolean -> edit.putBoolean(key, value)
            is String -> edit.putString(key, value)
            is Int -> edit.putInt(key, value)
            is Long -> edit.putLong(key, value)
            is ComponentName -> edit.putString(key, value.flattenToString())
            else -> AppLog.e("Unknown value $value for key $key")
        }
    }
}
