package info.anodsplace.carwidget.content.preferences

import android.annotation.SuppressLint
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

    suspend fun dropWidgetSettings(db: ShortcutsDatabase, context: Context, appWidgetIds: IntArray) = withContext(Dispatchers.IO) {
        if (appWidgetIds.isNotEmpty()) {
            db.deleteTargets(appWidgetIds.asList())
        }

        for (appWidgetId in appWidgetIds) {
            val prefName = String.format(Locale.US, PREF_NAME, appWidgetId)
            val filePath = context.filesDir?.parent + String.format(Locale.US, SHARED_PREFS_PATH, prefName)
            AppLog.i("Drop widget file: $filePath")
            val file = File(filePath)
            file.delete()
        }
    }

    fun isDbMigrated(context: Context, appWidgetId: Int): Boolean {
        val prefs = getSharedPreferences(context, appWidgetId)
        if (prefs.contains("db_migrated")) {
            return true
        }
        for (i in 0 until LAUNCH_COMPONENT_NUMBER_MAX) {
            val key = getLaunchComponentKey(i)
            val id = prefs.getLong(key, WidgetInterface.idUnknown)
            if (id != WidgetInterface.idUnknown) {
                return false
            }
        }
        return true
    }

    @SuppressLint("ApplySharedPref")
    fun launcherComponentsMigrated(context: Context, appWidgetId: Int) {
        val prefs = getSharedPreferences(context, appWidgetId)
        val edit = prefs.edit()
        for (i in 0 until LAUNCH_COMPONENT_NUMBER_MAX) {
            val key = getLaunchComponentKey(i)
            edit.remove(key)
        }
        edit.putBoolean("db_migrated", true)
        edit.commit()
    }

    fun getMigrateIds(context: Context, appWidgetId: Int): ArrayList<Long> {
        val prefs = getSharedPreferences(context, appWidgetId)
        val ids = ArrayList<Long>(LAUNCH_COMPONENT_NUMBER_MAX)
        for (i in 0 until LAUNCH_COMPONENT_NUMBER_MAX) {
            val key = getLaunchComponentKey(i)
            val id = prefs.getLong(key, WidgetInterface.idUnknown)
            ids.add(i, id)
        }
        return ids
    }
}
