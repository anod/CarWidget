package com.anod.car.home.prefs

import android.content.Intent
import android.os.Bundle

import com.anod.car.home.R
import com.anod.car.home.model.NotificationShortcutsModel
import com.anod.car.home.prefs.PickShortcutUtils.PreferenceKey
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.prefs.views.ShortcutPreference

class ConfigurationNotificationShortcuts : ConfigurationPreferenceFragment(), PreferenceKey, ShortcutPreference.DropCallback {

    private val pickShortcutUtils: PickShortcutUtils by lazy { PickShortcutUtils(this, model, this) }
    private val model: NotificationShortcutsModel by lazy { NotificationShortcutsModel.init(context!!) }

    override val isAppWidgetIdRequired: Boolean
        get() = false

    override val xmlResource: Int
        get() = R.xml.preference_notif_shortcuts

    override val sharedPreferencesName: String
        get() = InCarStorage.PREF_NAME

    override fun onCreateImpl(savedInstanceState: Bundle?) {

        pickShortcutUtils.onRestoreInstanceState(savedInstanceState)

        for (i in 0 until InCarStorage.NOTIFICATION_COMPONENT_NUMBER) {
            val p = findPreference(getInitialKey(i)) as ShortcutPreference
            pickShortcutUtils.initLauncherPreference(i, p)
            p.setDropCallback(this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pickShortcutUtils.onSaveInstanceState(outState)
    }

    override fun getInitialKey(position: Int): String {
        return InCarStorage.getNotifComponentName(position)
    }

    override fun getCompiledKey(position: Int): String {
        return InCarStorage.getNotifComponentName(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pickShortcutUtils.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onScrollRequest(top: Int): Int {
        return 0
    }

    override fun onDrop(oldCellId: Int, newCellId: Int): Boolean {
        if (oldCellId == newCellId) {
            return false
        }
        model.move(oldCellId, newCellId)
        refreshShortcuts()
        return true
    }

    private fun refreshShortcuts() {
        model.init()
        for (i in 0 until InCarStorage.NOTIFICATION_COMPONENT_NUMBER) {
            val key = getCompiledKey(i)
            val p = findPreference(key) as ShortcutPreference
            pickShortcutUtils.refreshPreference(p)
        }
    }
}
