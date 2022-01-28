package info.anodsplace.carwidget.content.preferences

import android.content.Context
import android.content.SharedPreferences

import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File
import java.util.ArrayList
import java.util.Locale

object WidgetStorage {

    const val LAUNCH_COMPONENT_NUMBER_MAX = 10
    const val LAUNCH_COMPONENT_NUMBER_DEFAULT = 6

    internal const val CMP_NUMBER = "cmp-number"
    private const val LAUNCH_COMPONENT = "launch-component-%d"

    const val PREF_NAME = "widget-%d"
    private const val SHARED_PREFS_PATH = "/shared_prefs/%s.xml"

    fun load(context: Context, defaultsProvider: WidgetSettings.DefaultsProvider, appWidgetId: Int): WidgetSettings {
        val prefs = getSharedPreferences(context, appWidgetId)
        return WidgetSettings(prefs, defaultsProvider)
    }

    private fun getSharedPreferences(context: Context, appWidgetId: Int): SharedPreferences {
        val prefName = String.format(Locale.US, PREF_NAME, appWidgetId)
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    private fun getLaunchComponentKey(position: Int): String {
        return String.format(Locale.US, LAUNCH_COMPONENT, position)
    }

    fun getLauncherComponents(context: Context, appWidgetId: Int,
                              count: Int): ArrayList<Long> {
        val prefs = getSharedPreferences(context, appWidgetId)
        val ids = ArrayList<Long>(count)
        for (i in 0 until count) {
            val key = getLaunchComponentKey(i)
            val id = prefs.getLong(key, WidgetInterface.idUnknown)
            ids.add(i, id)
        }
        return ids
    }

    suspend fun saveShortcut(db: ShortcutsDatabase, context: Context, shortcutId: Long, cellId: Int, appWidgetId: Int) {
        val key = getLaunchComponentKey(cellId)
        val prefs = getSharedPreferences(context, appWidgetId)
        saveShortcutId(db, prefs, shortcutId, key)
    }

    suspend fun saveShortcutId(db: ShortcutsDatabase, preferences: SharedPreferences, shortcutId: Long, key: String) {
        val curShortcutId = preferences.getLong(key, WidgetInterface.idUnknown)
        if (curShortcutId != WidgetInterface.idUnknown) {
            db.deleteItemFromDatabase(curShortcutId)
        }
        val editor = preferences.edit()
        editor.putLong(key, shortcutId)
        editor.apply()
    }

    suspend fun dropWidgetSettings(db: ShortcutsDatabase, context: Context, appWidgetIds: IntArray) = withContext(Dispatchers.IO) {
        for (appWidgetId in appWidgetIds) {
            val prefs = getSharedPreferences(context, appWidgetId)

            for (i in 0 until LAUNCH_COMPONENT_NUMBER_MAX) {
                val key = getLaunchComponentKey(i)
                val curShortcutId = prefs.getLong(key, WidgetInterface.idUnknown)
                if (curShortcutId != WidgetInterface.idUnknown) {
                    db.deleteItemFromDatabase(curShortcutId)
                }
            }

            val prefName = String.format(Locale.US, PREF_NAME, appWidgetId)
            val filePath = context.filesDir?.parent + String.format(Locale.US, SHARED_PREFS_PATH, prefName)
            AppLog.i("Drop widget file: $filePath")
            val file = File(filePath)
            file.delete()
        }
    }

    fun dropShortcutPreference(cellId: Int, appWidgetId: Int, context: Context) {
        val prefs = getSharedPreferences(context, appWidgetId)
        val edit = prefs.edit()
        val key = getLaunchComponentKey(cellId)
        edit.remove(key)
        edit.apply()
    }
}
