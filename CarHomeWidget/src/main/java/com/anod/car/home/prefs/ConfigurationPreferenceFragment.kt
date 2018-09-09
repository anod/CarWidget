package com.anod.car.home.prefs

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.anod.car.home.R
import com.anod.car.home.prefs.views.SeekBarDialogPreference
import com.anod.car.home.prefs.views.SeekBarPreferenceDialogFragment
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.Utils

/**
 * @author alex
 * @date 11/19/13
 */
abstract class ConfigurationPreferenceFragment : PreferenceFragmentCompat() {
    protected var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    protected abstract val xmlResource: Int

    protected abstract val sharedPreferencesName: String

    protected open val isAppWidgetIdRequired: Boolean
        get() = true

    protected open val optionsMenuResource: Int
        get() = 0

    protected open fun onCreateImpl(savedInstanceState: Bundle?) { }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val res = optionsMenuResource
        if (res == 0) {
            super.onCreateOptionsMenu(menu, inflater)
            return
        }
        inflater!!.inflate(res, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isAppWidgetIdRequired) {
            appWidgetId = Utils.readAppWidgetId(savedInstanceState, activity!!.intent)
        }
        super.onCreate(savedInstanceState)
        if (optionsMenuResource > 0) {
            setHasOptionsMenu(true)
        }

        onCreateImpl(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = sharedPreferencesName
        addPreferencesFromResource(xmlResource)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (isAppWidgetIdRequired) {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val defaultResultValue = Intent()
                defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                activity!!.setResult(Activity.RESULT_OK, defaultResultValue)
            } else {
                AppLog.w("AppWidgetId required")
                activity!!.finish()
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isAppWidgetIdRequired) {
            outState.putInt("appWidgetId", appWidgetId)
        }
    }

    protected fun setIntent(key: String, cls: Class<*>, appWidgetId: Int) {
        val pref = findPreference(key)
        val intent = Intent(activity, cls)
        if (appWidgetId > 0) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        pref.intent = intent
    }

    protected fun showFragmentOnClick(key: String, fragmentCls: Class<*>) {
        val pref = findPreference(key)
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            (activity as ConfigurationActivity)
                    .startPreferencePanel(fragmentCls.name, preference)
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.apply) {
            (activity as ConfigurationActivity).onApplyClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is SeekBarDialogPreference) {
            if (fragmentManager!!.findFragmentByTag("android.support.v7.preference.PreferenceFragment.DIALOG") == null) {

                val f = SeekBarPreferenceDialogFragment.newInstance(preference.getKey())
                f.setTargetFragment(this, 0)
                f.show(this.fragmentManager!!, "android.support.v7.preference.PreferenceFragment.DIALOG")
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

}
