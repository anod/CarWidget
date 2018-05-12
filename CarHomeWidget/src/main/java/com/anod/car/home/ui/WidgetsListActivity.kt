package com.anod.car.home.ui

import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.drawer.NavigationDrawer
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.model.PrefsMigrate
import com.anod.car.home.utils.TrialDialogs
import com.anod.car.home.utils.Utils
import com.anod.car.home.utils.Version

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem

/**
 * @author alex
 * @date 5/24/13
 */
open class WidgetsListActivity : CarWidgetActivity() {

    private var wizardShown: Boolean = false
    private val drawer: NavigationDrawer by lazy { NavigationDrawer(this, 0) }
    private val version: Version by lazy { Version(this) }
    private var proDialogShown: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val largeWidgetIds = WidgetHelper.getLargeWidgetIds(this)

        PrefsMigrate.migrate(this, largeWidgetIds)

        drawer.setSelected(R.id.nav_widgets)

        if (savedInstanceState == null) {
            // to give support on lower android version, we are not calling getFragmentManager()
            val fm = supportFragmentManager

            // Create the list fragment and add it as our sole content.
            if (fm.findFragmentById(R.id.content_frame) == null) {
                val f = WidgetsListFragment.newInstance()
                fm.beginTransaction().add(R.id.content_frame, f).commit()
            }
        } else {
            wizardShown = savedInstanceState.getBoolean("wizard-shown")
            proDialogShown = savedInstanceState.getBoolean("dialog-shown")
        }

        if (!wizardShown) {
            if (version.isFree && Utils.isProInstalled(this)) {
                if (!proDialogShown) {
                    proDialogShown = true
                    TrialDialogs.buildProInstalledDialog(this).show()
                }
            }
            val isFreeInstalled = !version.isFree && Utils.isFreeInstalled(this)
            val allWidgtIds = WidgetHelper.getAllWidgetIds(this)
            if (allWidgtIds.isEmpty() && !isFreeInstalled) {
                wizardShown = true
                startWizard()
            }
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawer.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (drawer.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("wizard-shown", wizardShown)
        outState.putBoolean("dialog-shown", proDialogShown)
        super.onSaveInstanceState(outState)
    }

    private fun startWizard() {
        val intent = Intent(this, WizardActivity::class.java)
        startActivity(intent)
    }

    fun startConfigActivity(appWidgetId: Int) {
        val configIntent = Intent(this, LookAndFeelActivity::class.java)
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivity(configIntent)
    }
}