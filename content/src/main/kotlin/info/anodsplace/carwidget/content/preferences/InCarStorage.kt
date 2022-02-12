package info.anodsplace.carwidget.content.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import java.util.*

/**
 * @author algavris
 * @date 19/03/2016.
 */
object InCarStorage {
    const val NOTIFICATION_COMPONENT_NUMBER = 3
    private const val NOTIF_COMPONENT = "notif-component-%d"
    const val PREF_NAME = "incar"

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun load(context: Context): InCarSettings {
        val prefs = getSharedPreferences(context)
        return InCarSettings(prefs)
    }

    private fun getNotifComponentName(position: Int): String {
        return String.format(Locale.US, NOTIF_COMPONENT, position)
    }

    fun isDbMigrated(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        if (prefs.contains("db_migrated")) {
            return true
        }
        for (i in 0 until NOTIFICATION_COMPONENT_NUMBER) {
            val key = getNotifComponentName(i)
            val id = prefs.getLong(key, WidgetInterface.idUnknown)
            if (id != WidgetInterface.idUnknown) {
                return false
            }
        }
        return true
    }

    @SuppressLint("ApplySharedPref")
    fun launcherComponentsMigrated(context: Context) {
        val prefs = getSharedPreferences(context)
        val edit = prefs.edit()
        for (i in 0 until NOTIFICATION_COMPONENT_NUMBER) {
            val key = getNotifComponentName(i)
            edit.remove(key)
        }
        edit.putBoolean("db_migrated", true)
        edit.commit()
    }

    fun getMigrateIds(context: Context): ArrayList<Long> {
        val prefs = getSharedPreferences(context)
        val ids = ArrayList<Long>(NOTIFICATION_COMPONENT_NUMBER)
        for (i in 0 until NOTIFICATION_COMPONENT_NUMBER) {
            val key = getNotifComponentName(i)
            val id = prefs.getLong(key, WidgetInterface.idUnknown)
            ids.add(i, id)
        }
        return ids
    }
}
