package com.anod.car.home.main

import android.content.Intent
import android.os.Bundle
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import info.anodsplace.carwidget.appwidget.WidgetIds
import com.anod.car.home.databinding.ActivityMainBinding
import com.anod.car.home.incar.BroadcastService
import info.anodsplace.carwidget.incar.ScreenOrientation
import com.anod.car.home.utils.*
import com.google.android.material.snackbar.Snackbar
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.extentions.isServiceRunning
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.carwidget.incar.InCarStatus
import info.anodsplace.framework.permissions.*
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
    private val inCarSettings: InCarInterface by inject()
    private val inCarStatus: InCarStatus by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
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
            val appWidgetIds = widgetIds.getAllWidgetIds()
            if (appWidgetIds.isEmpty() && !isFreeInstalled) {
                wizardShown = true
                startActivity(Intent(this, WizardActivity::class.java))
            }
        }

        if (!wizardShown && !proDialogShown) {
            val permissions = requestPermissions()
            if (permissions.isNotEmpty()) {
                RequestPermissionsActivity.start(this, permissions.toTypedArray(), requestPermissionsResult)
            }
        }
        AppLog.d("BroadcastService is running: ${isServiceRunning(BroadcastService::class.java)}")
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

    private fun requestPermissions(): List<String> {
        if (!inCarStatus.isEnabled) {
            return emptyList()
        }
        val permissions = mutableListOf<String>()
        if (inCarSettings.screenOrientation != ScreenOrientation.DISABLED && AppPermissions.shouldShowMessage(this, CanDrawOverlay)) {
            permissions.add(CanDrawOverlay.value)
        }
        val needsWritePermission = inCarSettings.brightness != InCarInterface.BRIGHTNESS_DISABLED
        if (needsWritePermission && AppPermissions.shouldShowMessage(this, WriteSettings)) {
            permissions.add(WriteSettings.value)
        }
        if (inCarSettings.autoAnswer != InCarInterface.AUTOANSWER_DISABLED) {
            if (AppPermissions.shouldShowMessage(this, AnswerPhoneCalls)) {
                permissions.add(AnswerPhoneCalls.value)
            }
        }
        if (inCarSettings.isActivityRequired) {
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

    companion object {
        const val requestPermissionsResult = 1
        const val extraInCarTab = "EXTRA_INCAR"
    }
}