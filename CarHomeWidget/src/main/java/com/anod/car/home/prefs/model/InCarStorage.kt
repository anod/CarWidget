package com.anod.car.home.prefs.model

import android.content.Context
import android.content.SharedPreferences

import com.anod.car.home.model.Shortcut

import java.util.ArrayList
import java.util.Locale

/**
 * @author algavris
 * @date 19/03/2016.
 */
object InCarStorage {
    const val NOTIFICATION_COMPONENT_NUMBER = 3
    private const val MODE_FORCE_STATE = "mode-force-state"
    private const val NOTIF_COMPONENT = "notif-component-%d"
    const val PREF_NAME = "incar"

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun load(context: Context): InCarSettings {
        val prefs = getSharedPreferences(context)
        return InCarSettings(prefs)
    }

    fun saveScreenTimeout(disabled: Boolean, disableCharging: Boolean, prefs: InCarSettings) {
        prefs.isDisableScreenTimeout = disabled
        prefs.isDisableScreenTimeoutCharging = disableCharging
        prefs.apply()
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

    fun saveNotifShortcut(context: Context, shortcutId: Long, position: Int) {
        saveNotifShortcut(context, getSharedPreferences(context), shortcutId, position)
    }

    private fun saveNotifShortcut(context: Context, prefs: SharedPreferences, shortcutId: Long, position: Int) {
        val key = getNotifComponentName(position)
        WidgetStorage.saveShortcutId(context, prefs, shortcutId, key)
    }

    private fun getNotifComponents(prefs: SharedPreferences): ArrayList<Long> {
        val ids = ArrayList<Long>(NOTIFICATION_COMPONENT_NUMBER)
        for (i in 0 until NOTIFICATION_COMPONENT_NUMBER) {
            val key = getNotifComponentName(i)
            val id = prefs.getLong(key, Shortcut.idUnknown)
            ids.add(i, id)
        }
        return ids
    }

}
