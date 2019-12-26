package com.anod.car.home.main

import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.prefs.LookAndFeelActivity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.anod.car.home.incar.ScreenOrientation
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.utils.*
import com.google.android.material.snackbar.Snackbar
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
            supportFragmentManager.commit {
                replace(R.id.content, WidgetsListFragment())
            }
        } else {
            wizardShown = savedInstanceState.getBoolean("wizard-shown")
            proDialogShown = savedInstanceState.getBoolean("dialog-shown")
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_widgets -> {
                    supportFragmentManager.commit {
                        replace(R.id.content, WidgetsListFragment())
                    }
                    true
                }
                R.id.nav_info -> {
                    supportFragmentManager.commit {
                        replace(R.id.content, AboutFragment())
                    }
                    true
                }
                R.id.nav_incar -> {
                    supportFragmentManager.commit {
                        replace(R.id.content, ConfigurationInCar())
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
                startActivity(Intent(this, WizardActivity::class.java))
            }
        }

        if (!wizardShown && !proDialogShown) {
            val appWidgetIds = WidgetHelper.getAllWidgetIds(this)
            if (appWidgetIds.isNotEmpty()) {
                val permissions = requestPermissions(appWidgetIds)
                if (permissions.isNotEmpty()) {
                    RequestPermissionsActivity.start(this, permissions.toTypedArray(), requestPermissionsResult)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestPermissionsResult) {
            if (resultCode == RequestPermissionsActivity.resultPermissionDenied) {
                Snackbar
                    .make(content, R.string.permissions_denied_open_settings, Snackbar.LENGTH_LONG)
                    .setAction(R.string.settings) {
                        startActivity(Intent().forAppSettings(this@WidgetsListActivity))
                    }
                    .show()
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
        if (settings.screenOrientation != ScreenOrientation.DISABLED && AppPermissions.shouldShowMessage(this, CanDrawOverlay)) {
            permissions.add(CanDrawOverlay.value)
        }
        val needsWritePermission = settings.brightness != InCarInterface.BRIGHTNESS_DISABLED || settings.isSamsungDrivingMode
        if (needsWritePermission && AppPermissions.shouldShowMessage(this, WriteSettings)) {
            permissions.add(WriteSettings.value)
        }
        if (settings.autoAnswer != InCarInterface.AUTOANSWER_DISABLED) {
            if (AppPermissions.shouldShowMessage(this, AnswerPhoneCalls)) {
                permissions.add(AnswerPhoneCalls.value)
            }
        }

        return permissions
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("wizard-shown", wizardShown)
        outState.putBoolean("dialog-shown", proDialogShown)
        super.onSaveInstanceState(outState)
    }

    fun startConfigActivity(appWidgetId: Int) {
        val configIntent = Intent(this, LookAndFeelActivity::class.java)
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivity(configIntent)
    }

    fun showInCarSettings() {
        supportFragmentManager.commit {
            replace(R.id.content, AboutFragment())
        }
        bottomNavigation.selectedItemId = R.id.nav_incar
    }

    companion object {
        const val requestPermissionsResult = 1
    }
}