package com.anod.car.home.prefs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.incar.BroadcastService
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation
import com.anod.car.home.prefs.views.ViewScreenTimeout
import com.anod.car.home.utils.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.app.DialogCustom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigurationInCar : ConfigurationPreferenceFragment() {

    private lateinit var activityRecognitionRequest: ActivityResultLauncher<Void>
    private var trialsLeft: Int = 0
    private var trialMessageShown: Boolean = false

    override val isAppWidgetIdRequired: Boolean
        get() = false

    override val xmlResource: Int
        get() = R.xml.preference_incar

    override val sharedPreferencesName: String
        get() = info.anodsplace.carwidget.content.preferences.InCarStorage.PREF_NAME

    private val broadcastServiceSwitchListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (serviceRequiredKeys.contains(key) && context != null) {
            val incar = info.anodsplace.carwidget.content.preferences.InCarStorage.load(requireContext())
            if (incar.isInCarEnabled && BroadcastService.isServiceRequired(incar)) {
                BroadcastService.startService(requireContext())
            } else {
                BroadcastService.stopService(requireContext())
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val sharedPrefs = info.anodsplace.carwidget.content.preferences.InCarStorage.getSharedPreferences(requireActivity())
        sharedPrefs.registerOnSharedPreferenceChangeListener(broadcastServiceSwitchListener)
    }

    override fun onDetach() {
        super.onDetach()
        val sharedPrefs = info.anodsplace.carwidget.content.preferences.InCarStorage.getSharedPreferences(requireActivity())
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(broadcastServiceSwitchListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRecognitionRequest = AppPermissions.register(this, ActivityRecognition) { isGranted ->
            if (isGranted) {
                val pref: CheckBoxPreference = findPreference(info.anodsplace.carwidget.content.preferences.InCarSettings.ACTIVITY_RECOGNITION)!!
                pref.isChecked = true
            }
        }
        val version = Version(requireActivity())

        initInCar()
        if (version.isFree) {
            trialsLeft = version.trialTimesLeft
            showTrialDialog()
        }
    }

    private fun createTrialDialog(): Dialog {
        return if (Utils.isProInstalled(requireActivity())) {
            TrialDialogs.buildProInstalledDialog(requireActivity())
        } else {
            trialMessageShown = true
            TrialDialogs.buildTrialDialog(trialsLeft, requireActivity())
        }
    }

    private fun showTrialDialog() {
        if (!trialMessageShown || trialsLeft == 0) {
            createTrialDialog().show()
        }
    }

    private fun initInCar() {
        val incar = info.anodsplace.carwidget.content.preferences.InCarStorage.load(requireActivity())
        val incarSwitch: SwitchPreferenceCompat = findPreference(info.anodsplace.carwidget.content.preferences.InCarSettings.INCAR_MODE_ENABLED)!!

        val allWidgetIds = WidgetHelper.getAllWidgetIds(requireActivity())
        if (allWidgetIds.isEmpty()) {
            incarSwitch.isEnabled = false
            incarSwitch.setSummary(R.string.please_add_widget)
        } else {
            incarSwitch.isEnabled = true
            incarSwitch.summary = ""
        }

        incarSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                BroadcastService.startService(requireActivity())
            } else {
                BroadcastService.stopService(requireActivity())
            }
            true
        }

        initActivityRecognition()
        initScreenTimeout(incar)
        initScreenOrientation()
        initBrightness()
        initAutoAnswer()

        setIntent(SCREEN_BT_DEVICE, BluetoothDeviceActivity::class.java, 0)
        showFragmentOnClick(MEDIA_SCREEN, ConfigurationInCarVolume::class.java)
        showFragmentOnClick(MORE_SCREEN, ConfigurationInCarMore::class.java)
        showFragmentOnClick(PREF_NOTIF_SHORTCUTS, ConfigurationNotificationShortcuts::class.java)
    }

    private fun initAutoAnswer() {
        val pref: ListPreference = findPreference("auto_answer")!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue != info.anodsplace.carwidget.content.preferences.InCarInterface.AUTOANSWER_DISABLED) {
                if (!AppPermissions.isGranted(requireContext(), AnswerPhoneCalls)) {
                    Toast.makeText(context, R.string.allow_answer_phone_calls, Toast.LENGTH_LONG).show()
                    AppPermissions.requestAnswerPhoneCalls(this, requestAnswerPhone)
                }
            }
            true
        }
    }

    private fun initBrightness() {
        val pref: ListPreference = findPreference("brightness")!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue != info.anodsplace.carwidget.content.preferences.InCarInterface.BRIGHTNESS_DISABLED) {
                if (!AppPermissions.isGranted(requireContext(), WriteSettings)) {
                    Toast.makeText(context, R.string.allow_permissions_brightness, Toast.LENGTH_LONG).show()
                    AppPermissions.requestWriteSettings(this, requestWriteSettings)
                }
            }
            true
        }
    }

    private fun initScreenOrientation() {
        val pref: ListPreference = findPreference("screen-orientation")!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue != ScreenOrientation.DISABLED.toString()) {
                if (!AppPermissions.isGranted(requireContext(), CanDrawOverlay)) {
                    Toast.makeText(context, R.string.allow_permission_overlay, Toast.LENGTH_LONG).show()
                    AppPermissions.requestDrawOverlay(this, requestDrawOverlay)
                }
            }
            true
        }
    }

    private fun initScreenTimeout(incar: info.anodsplace.carwidget.content.preferences.InCarSettings) {
        val pref: Preference = requirePreference(SCREEN_TIMEOUT_LIST)
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            DialogCustom(
                    requireContext(),
                    App.theme(requireContext()).alert,
                    R.string.pref_screen_timeout,
                    R.layout.dialog_screen_timeout
            ) { view, _ ->
                val v = ViewScreenTimeout(view)
                v.screenOnSwitch.isChecked = incar.isDisableScreenTimeout
                v.whileCharging.isChecked = incar.isDisableScreenTimeoutCharging
                v.useAlertGroup.isVisible = AlertWindow.isSupported
                v.useAlert.isChecked = incar.screenOnAlert.enabled
                v.onStateChange { keepOn, whileCharging, useAlert ->
                    info.anodsplace.carwidget.content.preferences.InCarStorage.saveScreenTimeout(keepOn, disableCharging = whileCharging, prefs = incar)
                    if (useAlert && AlertWindow.isSupported) {
                        incar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(true, incar.screenOnAlert)
                        if (!AlertWindow.hasPermission(requireContext())) {
                            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + requireContext().packageName)))
                        }
                    } else {
                        incar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(false, incar.screenOnAlert)
                    }
                    incar.apply()
                }
            }.show()
            true
        }
    }

    private fun initActivityRecognition() {
        val pref: Preference = findPreference(info.anodsplace.carwidget.content.preferences.InCarSettings.ACTIVITY_RECOGNITION)!!
        lifecycleScope.launch {
            val status = withContext(Dispatchers.Default) { GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireActivity()) }
            val summary = renderPlayServiceStatus(status)
            updateActivityRecognition(status, summary, pref)
        }
    }

    private fun updateActivityRecognition(status: Int, summary: String, pref: Preference) {
        pref.summary = summary
        if (status != ConnectionResult.SUCCESS) {
            pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                val d = GoogleApiAvailability.getInstance().getErrorDialog(activity, status, PS_DIALOG_REQUEST_CODE)
                d.show()
                false
            }
        } else {
            if (AppPermissions.shouldShowMessage(requireActivity(), ActivityRecognition)) {
                pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue == true) {
                        if (!AppPermissions.isGranted(requireContext(), ActivityRecognition)) {
                            activityRecognitionRequest.launch(null)
                            return@OnPreferenceChangeListener false
                        }
                    }
                    true
                }
            }
        }
    }

    private fun renderPlayServiceStatus(errorCode: Int): String {
        if (errorCode == ConnectionResult.SUCCESS) {
            return getString(R.string.use_gms_for_activity)
        }
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            return getString(R.string.gms_service_missing)
        }
        if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            return getString(R.string.gms_service_update_required)
        }
        if (errorCode == ConnectionResult.SERVICE_DISABLED) {
            return getString(R.string.gms_service_disabled)
        }
        return if (errorCode == ConnectionResult.SERVICE_INVALID) {
            getString(R.string.gms_service_invalid)
        } else GoogleApiAvailability.getInstance().getErrorString(errorCode)
    }

    companion object {
        private const val MEDIA_SCREEN = "media-screen"
        private const val MORE_SCREEN = "more-screen"
        private const val SCREEN_BT_DEVICE = "bt-device-screen"
        private const val PREF_NOTIF_SHORTCUTS = "notif-shortcuts"
        const val PS_DIALOG_REQUEST_CODE = 4
        const val SCREEN_TIMEOUT_LIST = "screen-timeout-list"
        const val requestDrawOverlay = 7
        const val requestWriteSettings = 8
        const val requestAnswerPhone = 9

        private val serviceRequiredKeys = arrayOf(
                info.anodsplace.carwidget.content.preferences.InCarSettings.HEADSET_REQUIRED,
                info.anodsplace.carwidget.content.preferences.InCarSettings.POWER_REQUIRED,
                info.anodsplace.carwidget.content.preferences.InCarSettings.CAR_DOCK_REQUIRED,

                info.anodsplace.carwidget.content.preferences.InCarSettings.POWER_BT_ENABLE,
                info.anodsplace.carwidget.content.preferences.InCarSettings.POWER_BT_DISABLE,

                info.anodsplace.carwidget.content.preferences.InCarSettings.ACTIVITY_RECOGNITION
        )
    }
}