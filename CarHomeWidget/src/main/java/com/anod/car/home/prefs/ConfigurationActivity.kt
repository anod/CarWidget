package com.anod.car.home.prefs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import android.view.MenuItem

import com.anod.car.home.appwidget.Provider
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.utils.Utils


class ConfigurationActivity : CarWidgetActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    private var appWidgetId: Int = 0

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        appWidgetId = Utils.readAppWidgetId(savedInstanceState, intent)

        if (savedInstanceState == null) {

            val conf = createFragmentInstance()
            conf.arguments = intent.extras
            supportFragmentManager.beginTransaction().add(android.R.id.content, conf).commit()
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Utils.saveAppWidgetId(outState, appWidgetId)
    }

    private fun createFragmentInstance(): androidx.fragment.app.Fragment {
        val intent = intent
        val extras = intent.extras
        val fragmentClass = extras!!.get(EXTRA_FRAGMENT) as Class<*>
        val fragmentClassName = fragmentClass.name
        return androidx.fragment.app.Fragment.instantiate(this, fragmentClassName, Bundle())
    }

    fun onApplyClick() {
        supportFragmentManager.popBackStack()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, preference: Preference): Boolean {

        return false
    }

    fun startPreferencePanel(fragmentClass: String, pref: Preference) {
        startPreferencePanel(fragmentClass, pref.title, null, 0)
    }

    private fun startPreferencePanel(fragmentClass: String, titleText: CharSequence?,
                                     resultTo: androidx.fragment.app.Fragment?, resultRequestCode: Int) {
        val args = Bundle()
        val f = androidx.fragment.app.Fragment.instantiate(this, fragmentClass, args)
        if (resultTo != null) {
            f.setTargetFragment(resultTo, resultRequestCode)
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, f)
        if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText)
        }
        transaction.setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.addToBackStack(BACK_STACK_PREFS)
        transaction.commitAllowingStateLoss()
    }

    override fun onPreferenceStartScreen(preferenceFragmentCompat: PreferenceFragmentCompat, preferenceScreen: PreferenceScreen): Boolean {
        preferenceFragmentCompat.preferenceScreen = preferenceScreen
        return true
    }

    override fun onBackPressed() {
        Provider.requestUpdate(this, intArrayOf())
        super.onBackPressed()
    }

    companion object {

        private const val BACK_STACK_PREFS = ":carwidget:prefs"
        const val EXTRA_FRAGMENT = "fragment"

        fun createFragmentIntent(context: Context, fragment: Class<*>): Intent {
            val intent = Intent(context, ConfigurationActivity::class.java)
            intent.putExtra(EXTRA_FRAGMENT, fragment)
            return intent
        }
    }

}