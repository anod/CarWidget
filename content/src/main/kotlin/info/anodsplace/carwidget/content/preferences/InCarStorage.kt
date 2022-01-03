package info.anodsplace.carwidget.content.preferences

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

    fun getNotifComponentName(position: Int): String {
        return String.format(Locale.US, NOTIF_COMPONENT, position)
    }

    fun dropNotifShortcut(position: Int, context: Context) {
        val key = getNotifComponentName(position)
        val prefs = getSharedPreferences(context)
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }

    fun getNotifComponents(context: Context): ArrayList<Long> {
        return getNotifComponents(getSharedPreferences(context))
    }

    fun saveNotifShortcut(db: ShortcutsDatabase, context: Context, shortcutId: Long, position: Int) {
        saveNotifShortcut(db, getSharedPreferences(context), shortcutId, position)
    }

    private fun saveNotifShortcut(db: ShortcutsDatabase, prefs: SharedPreferences, shortcutId: Long, position: Int) {
        val key = getNotifComponentName(position)
        WidgetStorage.saveShortcutId(db, prefs, shortcutId, key)
    }

    private fun getNotifComponents(prefs: SharedPreferences): ArrayList<Long> {
        val ids = ArrayList<Long>(NOTIFICATION_COMPONENT_NUMBER)
        for (i in 0 until NOTIFICATION_COMPONENT_NUMBER) {
            val key = getNotifComponentName(i)
            val id = prefs.getLong(key, WidgetInterface.idUnknown)
            ids.add(i, id)
        }
        return ids
    }

}
