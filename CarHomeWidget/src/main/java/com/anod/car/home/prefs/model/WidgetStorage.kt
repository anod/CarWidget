package com.anod.car.home.prefs.model

import android.content.Context
import android.content.SharedPreferences

import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.ShortcutModel
import info.anodsplace.framework.AppLog

import java.io.File
import java.util.ArrayList
import java.util.Locale

object WidgetStorage {

    private const val LAUNCH_COMPONENT_NUMBER_MAX = 10
    private const val LAUNCH_COMPONENT_NUMBER_DEFAULT = 6

    const val CMP_NUMBER = "cmp-number"
    private const val LAUNCH_COMPONENT = "launch-component-%d"

    const val PREF_NAME = "widget-%d"
    private const val SHARED_PREFS_PATH = "/shared_prefs/%s.xml"

    fun getSharedPreferences(context: Context, appWidgetId: Int): SharedPreferences {
        val prefName = String.format(Locale.US, PREF_NAME, appWidgetId)
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    fun load(context: Context, appWidgetId: Int): WidgetSettings {
        val prefs = getSharedPreferences(context, appWidgetId)
        return WidgetSettings(prefs, context.resources)
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
            val id = prefs.getLong(key, Shortcut.idUnknown)
            ids.add(i, id)
        }
        return ids
    }

    fun getLaunchComponentNumber(context: Context, appWidgetId: Int): Int {
        val prefs = getSharedPreferences(context, appWidgetId)
        val num = prefs
                .getInt(CMP_NUMBER, prefs.getInt("cmp-number", LAUNCH_COMPONENT_NUMBER_DEFAULT))
        return if (num == 0) LAUNCH_COMPONENT_NUMBER_DEFAULT else num
    }

    fun saveLaunchComponentNumber(count: Int?, context: Context, appWidgetId: Int) {
        val prefs = getSharedPreferences(context, appWidgetId)
        val edit = prefs.edit()
        edit.putInt(CMP_NUMBER, count!!)
        edit.apply()
    }

    fun saveShortcut(context: Context, shortcutId: Long, cellId: Int, appWidgetId: Int) {
        val key = getLaunchComponentKey(cellId)
        val prefs = getSharedPreferences(context, appWidgetId)
        saveShortcutId(context, prefs, shortcutId, key)
    }

    fun saveShortcutId(context: Context, preferences: SharedPreferences, shortcutId: Long, key: String) {
        val curShortcutId = preferences.getLong(key, Shortcut.idUnknown)
        if (curShortcutId != Shortcut.idUnknown) {
            val model = ShortcutModel(context)
            model.deleteItemFromDatabase(curShortcutId)
        }
        val editor = preferences.edit()
        editor.putLong(key, shortcutId)
        editor.apply()
    }

    fun dropWidgetSettings(context: Context, appWidgetIds: IntArray) {
        val model = ShortcutModel(context)
        for (appWidgetId in appWidgetIds) {
            val prefs = getSharedPreferences(context, appWidgetId)

            for (i in 0 until LAUNCH_COMPONENT_NUMBER_MAX) {
                val key = getLaunchComponentKey(i)
                val curShortcutId = prefs.getLong(key, Shortcut.idUnknown)
                if (curShortcutId != Shortcut.idUnknown) {
                    model.deleteItemFromDatabase(curShortcutId)
                }
            }

            val prefName = String.format(Locale.US, PREF_NAME, appWidgetId)
            val filePath = context.filesDir.parent + String.format(Locale.US, WidgetStorage.SHARED_PREFS_PATH, prefName)
            AppLog.d("Drop widget file: $filePath")
            val file = File(filePath)
            file.delete()
        }
    }

    fun hasSettingsFile(context: Context, appWidgetId: Int): Boolean {
        val prefName = String.format(Locale.US, PREF_NAME, appWidgetId)
        val filePath = context.filesDir.parent + String.format(Locale.US, WidgetStorage.SHARED_PREFS_PATH, prefName)
        val file = File(filePath)
        return file.isFile
    }

    fun dropShortcutPreference(cellId: Int, appWidgetId: Int, context: Context) {
        val prefs = getSharedPreferences(context, appWidgetId)
        val edit = prefs.edit()
        val key = getLaunchComponentKey(cellId)
        edit.remove(key)
        edit.apply()
    }
}
