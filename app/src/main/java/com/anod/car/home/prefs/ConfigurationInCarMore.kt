package com.anod.car.home.prefs

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.anod.car.home.R
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.preferences.InCarStorage

class ConfigurationInCarMore : ConfigurationPreferenceFragment() {
    private val prefs: InCarSettings by lazy { InCarStorage.load(requireActivity()) }

    override val isAppWidgetIdRequired: Boolean
        get() = false

    override val xmlResource: Int
        get() = R.xml.preference_incar_more

    override val sharedPreferencesName: String
        get() = InCarStorage.PREF_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAutorunApp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_APPLICATION) {
            saveAutorunApp(data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveAutorunApp(data: Intent?) {
        var component: ComponentName? = null
        if (data != null) {
            component = data.component
        }
        // update storage
        prefs.autorunApp = component
        prefs.apply()
        updateAutorunAppPref(data)
    }

    private fun initAutorunApp() {
        val pref = requirePreference(AUTORUN_APP_PREF) as ListPreference
        val autorunApp = prefs.autorunApp
        if (autorunApp == null) {
            updateAutorunAppPref(null)
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.component = autorunApp
            updateAutorunAppPref(intent)
        }
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val selection = newValue as String
            if (selection == AUTORUN_APP_DISABLED) {
                saveAutorunApp(null)
            } else {
                val mainIntent = Intent(activity, AllAppsActivity::class.java)
                startActivityForResult(mainIntent, REQUEST_PICK_APPLICATION)
            }
            false
        }
    }

    private fun updateAutorunAppPref(data: Intent?) {
        val pref = requirePreference(AUTORUN_APP_PREF) as ListPreference
        val title: String
        val value: String
        if (data == null) {
            title = getString(R.string.disabled)
            value = AUTORUN_APP_DISABLED
        } else {
            // get name
            val pm = requireActivity().packageManager
            val resolveInfo = pm.resolveActivity(data, 0)
            title = if (resolveInfo != null) {
                resolveInfo.activityInfo.loadLabel(pm) as String
            } else {
                data.component!!.packageName
            }
            value = AUTORUN_APP_CUSTOM
        }
        // update preference
        pref.summary = title
        pref.value = value
    }

    companion object {
        private const val AUTORUN_APP_PREF = "autorun-app-choose"
        private const val AUTORUN_APP_DISABLED = "disabled"
        private const val AUTORUN_APP_CUSTOM = "custom"
        private const val REQUEST_PICK_APPLICATION = 0
        private const val requestWriteSettings = 1
    }
}
