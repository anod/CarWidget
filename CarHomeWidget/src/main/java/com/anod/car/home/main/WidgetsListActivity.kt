package com.anod.car.home.main

import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.prefs.LookAndFeelActivity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.transaction
import com.anod.car.home.incar.ScreenOrientation
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.utils.*
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author alex
 * @date 5/24/13
 */
open class WidgetsListActivity : CarWidgetActivity() {

    private var wizardShown: Boolean = false
    private val version: Version by lazy { Version(this) }
    private var proDialogShown: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                replace(R.id.content_frame, WidgetsListFragment.newInstance())
            }
        } else {
            wizardShown = savedInstanceState.getBoolean("wizard-shown")
            proDialogShown = savedInstanceState.getBoolean("dialog-shown")
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_widgets -> {
                    supportFragmentManager.transaction {
                        replace(R.id.content_frame, WidgetsListFragment.newInstance())
                    }
                    true
                }
                R.id.nav_info -> {
                    supportFragmentManager.transaction {
                        replace(R.id.content_frame, AboutFragment())
                    }
                    true
                }
                R.id.nav_incar -> {
                    supportFragmentManager.transaction {
                        replace(R.id.content_frame, ConfigurationInCar())
                    }
                    true
                }
                else -> false
            }
        }


        if (!wizardShown) {
            if (version.isFree && Utils.isProInstalled(this)) {
                if (!proDialogShown) {
                    proDialogShown = true
                    TrialDialogs.buildProInstalledDialog(this).show()
                }
            }
            val isFreeInstalled = !version.isFree && Utils.isFreeInstalled(this)
            val appWidgetIds = WidgetHelper.getAllWidgetIds(this)
            if (appWidgetIds.isEmpty() && !isFreeInstalled) {
                wizardShown = true
                startWizard()
            }
        }

        if (!wizardShown && !proDialogShown) {
            val appWidgetIds = WidgetHelper.getAllWidgetIds(this)
            if (appWidgetIds.isNotEmpty()) {
                val permissions = requestPermissions(appWidgetIds)
                if (permissions.isNotEmpty()) {
                    RequestPermissionsActivity.start(this, permissions.toTypedArray())
                }
            }
        }
    }

    private fun requestPermissions(appWidgetIds: IntArray): List<String> {
        val settings = InCarStorage.load(this)
        val status = InCarStatus(appWidgetIds.size, version, settings)
        if (!status.isEnabled) {
            return emptyList()
        }
        val permissions = mutableListOf<String>()
        if (settings.screenOrientation != ScreenOrientation.DISABLED && !AppPermissions.isGranted(this, CanDrawOverlay)) {
            permissions.add(CanDrawOverlay.value)
        }
        if (settings.brightness != InCarInterface.BRIGHTNESS_DISABLED && !AppPermissions.isGranted(this, WriteSettings)) {
            permissions.add(WriteSettings.value)
        }
        if (settings.autoAnswer != InCarInterface.AUTOANSWER_DISABLED) {
            if (!AppPermissions.isGranted(this, AnswerPhoneCalls)) {
                permissions.add(AnswerPhoneCalls.value)
            }
            if (!AppPermissions.isGranted(this, ModifyPhoneState)) {
                permissions.add(ModifyPhoneState.value)
            }
        }

        return permissions
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

    fun showInCarSettings() {
        supportFragmentManager.transaction {
            replace(R.id.content_frame, AboutFragment())
        }
        bottomNavigation.selectedItemId = R.id.nav_incar
    }
}