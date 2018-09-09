package com.anod.car.home.prefs

import com.anod.car.home.R
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.Shortcuts
import com.anod.car.home.prefs.views.ShortcutPreference
import com.anod.car.home.utils.ShortcutPicker

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference

class PickShortcutUtils(private val configurationFragment: ConfigurationPreferenceFragment,
                        private val model: Shortcuts, private val preferenceKey: PreferenceKey) : ShortcutPicker.Handler {

    private val activity: ConfigurationActivity = configurationFragment.activity as ConfigurationActivity

    private val picker: ShortcutPicker = ShortcutPicker(model, this, activity)

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        configurationFragment.startActivityForResult(intent, requestCode)
    }

    override fun onAddShortcut(cellId: Int, info: Shortcut?) {
        if (info != null && info.id != Shortcut.idUnknown.toLong()) {
            val key = preferenceKey.getCompiledKey(cellId)
            val p = configurationFragment.findPreference(key) as ShortcutPreference
            refreshPreference(p)
        }
    }

    override fun onEditComplete(cellId: Int) {
        val key = preferenceKey.getCompiledKey(cellId)
        val p = configurationFragment.findPreference(key) as ShortcutPreference
        refreshPreference(p)
    }

    interface PreferenceKey {
        fun getInitialKey(position: Int): String
        fun getCompiledKey(position: Int): String
    }

    fun initLauncherPreference(position: Int, p: ShortcutPreference): ShortcutPreference {
        p.key = preferenceKey.getCompiledKey(position)
        p.shortcutPosition = position
        p.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            val pref = preference as ShortcutPreference
            val shortcutPosition = pref.shortcutPosition
            val info = model.get(shortcutPosition)
            if (info == null) {
                showActivityPicker(shortcutPosition)
            } else {
                startEditActivity(shortcutPosition, info.id)
            }
            true
        }
        p.deleteClickListener = Preference.OnPreferenceClickListener { preference ->
            val pref = preference as ShortcutPreference
            model.drop(pref.shortcutPosition)
            refreshPreference(pref)
            true
        }
        refreshPreference(p)
        return p
    }

    private fun showActivityPicker(position: Int) {
        picker.showActivityPicker(position)
    }


    private fun startEditActivity(cellId: Int, shortcutId: Long) {
        picker.showEditActivity(cellId, shortcutId, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    fun refreshPreference(pref: ShortcutPreference) {
        val cellId = pref.shortcutPosition
        val info = model.get(cellId)
        pref.setAppTheme(activity.app.themeIdx)
        if (info == null) {
            pref.setTitle(R.string.set_shortcut)
            pref.setIconResource(R.drawable.ic_add_shortcut_holo)
            pref.showButtons(false)
        } else {
            val icon = model.loadIcon(info.id)
            pref.setIconBitmap(icon.bitmap)
            pref.title = info.title
            pref.showButtons(true)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        picker.onActivityResult(requestCode, resultCode, data)
    }

    fun onSaveInstanceState(outState: Bundle) {
        picker.onSaveInstanceState(outState)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        picker.onRestoreInstanceState(savedInstanceState, null)
    }
}
