package com.anod.car.home.main

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import info.anodsplace.carwidget.appwidget.WidgetIds
import com.anod.car.home.databinding.ActivityMainBinding
import com.anod.car.home.incar.BroadcastService
import info.anodsplace.carwidget.incar.ScreenOrientation
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.utils.*
import com.google.android.material.snackbar.Snackbar
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.extentions.isServiceRunning
import info.anodsplace.carwidget.incar.InCarStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author alex
 * @date 5/24/13
 */
open class WidgetsListActivity : CarWidgetActivity(), KoinComponent {

    private lateinit var binding: ActivityMainBinding
    private var wizardShown: Boolean = false
    private val version: Version by lazy { Version(this) }
    private var proDialogShown: Boolean = false
    private val widgetIds: WidgetIds by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val openInCarTab = intent?.extras?.getBoolean(extraInCarTab) ?: false
            binding.bottomNavigation.selectedItemId = if (openInCarTab) R.id.nav_incar else R.id.nav_widgets
//            supportFragmentManager.commit {
//                replace(R.id.content, if (openInCarTab) ConfigurationInCar() else WidgetsListFragment())
//            }
        } else {
            wizardShown = savedInstanceState.getBoolean("wizard-shown")
            proDialogShown = savedInstanceState.getBoolean("dialog-shown")
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_widgets -> {
//                    supportFragmentManager.commit {
//                        replace(R.id.content, WidgetsListFragment())
//                    }
                    true
                }
                R.id.nav_info -> {
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
            val appWidgetIds = widgetIds.getAllWidgetIds()
            if (appWidgetIds.isEmpty() && !isFreeInstalled) {
                wizardShown = true
                startActivity(Intent(this, WizardActivity::class.java))
            }
        }

        if (!wizardShown && !proDialogShown) {
            val appWidgetIds = widgetIds.getAllWidgetIds()
            if (appWidgetIds.isNotEmpty()) {
                val permissions = requestPermissions(appWidgetIds)
                if (permissions.isNotEmpty()) {
                    RequestPermissionsActivity.start(this, permissions.toTypedArray(), requestPermissionsResult)
                }
            }
        }
        AppLog.d("BroadcastService is running: ${isServiceRunning(BroadcastService::class.java)}")
    }

    override fun onPause() {
        super.onPause()
        BroadcastService.registerBroadcastService(applicationContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestPermissionsResult) {
            if (resultCode == RequestPermissionsActivity.resultPermissionDenied) {
                Snackbar
                    .make(binding.content, R.string.permissions_denied_open_settings, Snackbar.LENGTH_LONG)
                    .setAction(R.string.settings) {
                        startActivity(Intent().forAppSettings(this@WidgetsListActivity))
                    }
                    .show()
            }
        }
    }

    private fun requestPermissions(appWidgetIds: IntArray): List<String> {
        val settings = info.anodsplace.carwidget.content.preferences.InCarStorage.load(this)
        val status = InCarStatus(appWidgetIds.size, version, settings)
        if (!status.isEnabled) {
            return emptyList()
        }
        val permissions = mutableListOf<String>()
        if (settings.screenOrientation != ScreenOrientation.DISABLED && AppPermissions.shouldShowMessage(this, CanDrawOverlay)) {
            permissions.add(CanDrawOverlay.value)
        }
        val needsWritePermission = settings.brightness != info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_DISABLED
        if (needsWritePermission && AppPermissions.shouldShowMessage(this, WriteSettings)) {
            permissions.add(WriteSettings.value)
        }
        if (settings.autoAnswer != info.anodsplace.carwidget.content.preferences.InCarInterface.AUTOANSWER_DISABLED) {
            if (AppPermissions.shouldShowMessage(this, AnswerPhoneCalls)) {
                permissions.add(AnswerPhoneCalls.value)
            }
        }
        if (settings.isActivityRequired) {
            if (AppPermissions.shouldShowMessage(this, ActivityRecognition)) {
                permissions.add(ActivityRecognition.value)
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
        binding.bottomNavigation.selectedItemId = R.id.nav_incar
    }

    companion object {
        const val requestPermissionsResult = 1
        const val extraInCarTab = "EXTRA_INCAR"
    }
}