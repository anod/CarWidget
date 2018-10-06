package com.anod.car.home.prefs

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference

import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.incar.ActivityRecognitionClientService
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.InCarSettings
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.utils.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import android.net.Uri
import android.content.Intent
import android.content.SharedPreferences
import com.anod.car.home.incar.ScreenOrientation
import com.anod.car.home.prefs.model.InCarInterface

class ConfigurationInCar : ConfigurationPreferenceFragment() {

    private var trialsLeft: Int = 0
    private var trialMessageShown: Boolean = false

    override val isAppWidgetIdRequired: Boolean
        get() = false

    override val xmlResource: Int
        get() = R.xml.preference_incar

    override val sharedPreferencesName: String
        get() = InCarStorage.PREF_NAME

    private val broadcastServiceSwitchListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (serviceRequiredKeys.contains(key) && context != null) {
            val incar = InCarStorage.load(context!!)
            if (incar.isInCarEnabled && BroadcastService.isServiceRequired(incar)) {
                BroadcastService.startService(context!!)
            } else {
                BroadcastService.stopService(context!!)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val sharedPrefs = InCarStorage.getSharedPreferences(activity!!)
        sharedPrefs.registerOnSharedPreferenceChangeListener(broadcastServiceSwitchListener)
    }

    override fun onDetach() {
        super.onDetach()
        val sharedPrefs = InCarStorage.getSharedPreferences(activity!!)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(broadcastServiceSwitchListener)
    }

    override fun onCreateImpl(savedInstanceState: Bundle?) {
        val version = Version(activity!!)

        initInCar()
        if (version.isFree) {
            trialsLeft = version.trialTimesLeft
            showTrialDialog()
        }
    }

    private fun createTrialDialog(): Dialog {
        if (Utils.isProInstalled(activity!!)) {
            return TrialDialogs.buildProInstalledDialog(activity!!)
        } else {
            trialMessageShown = true
            return TrialDialogs.buildTrialDialog(trialsLeft, activity!!)
        }
    }

    private fun showTrialDialog() {
        if (!trialMessageShown || trialsLeft == 0) {
            createTrialDialog().show()
        }
    }

    private fun initInCar() {
        val incar = InCarStorage.load(activity!!)

        val incarSwitch = findPreference(InCarSettings.INCAR_MODE_ENABLED)

        val allWidgetIds = WidgetHelper.getAllWidgetIds(activity!!)
        if (allWidgetIds.isEmpty()) {
            incarSwitch.isEnabled = false
            incarSwitch.setSummary(R.string.please_add_widget)
        } else {
            incarSwitch.isEnabled = true
            incarSwitch.summary = ""
        }

        incarSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                BroadcastService.startService(activity!!)
            } else {
                BroadcastService.stopService(activity!!)
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
        val pref = findPreference("auto_answer") as ListPreference
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue != InCarInterface.AUTOANSWER_DISABLED)
            {
                if (!AppPermissions.isGranted(context!!, AnswerPhoneCalls)) {
                    Toast.makeText(context, R.string.allow_answer_phone_calls, Toast.LENGTH_LONG).show()
                    AppPermissions.requestAnswerPhoneCalls(this, requestAnswerPhone)
                }
            }
            true
        }
    }

    private fun initBrightness() {
        val pref = findPreference("brightness") as ListPreference
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue != InCarInterface.BRIGHTNESS_DISABLED)
            {
                if (!AppPermissions.isGranted(context!!, WriteSettings)) {
                    Toast.makeText(context, R.string.allow_permissions_brightness, Toast.LENGTH_LONG).show()
                    AppPermissions.requestWriteSettings(this, requestWriteSettings)
                }
            }
            true
        }
    }

    private fun initScreenOrientation() {
        val pref = findPreference("screen-orientation") as ListPreference
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue != ScreenOrientation.DISABLED.toString())
            {
                if (!AppPermissions.isGranted(context!!, CanDrawOverlay)) {
                    Toast.makeText(context, R.string.allow_permission_overlay, Toast.LENGTH_LONG).show()
                    AppPermissions.requestDrawOverlay(this, requestDrawOverlay)
                }
            }
            true
        }
    }

    private fun initScreenTimeout(incar: InCarSettings) {
        val pref = findPreference(SCREEN_TIMEOUT_LIST) as ListPreference

        if (incar.isDisableScreenTimeout) {
            if (incar.isDisableScreenTimeoutCharging) {
                pref.value = "disabled-charging"
            } else {
                pref.value = "disabled"
            }
        } else {
            pref.value = "enabled"
        }

        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val value = newValue as String
            when (value) {
                "disabled-charging" -> {
                    InCarStorage.saveScreenTimeout(true, true, incar)
                    pref.value = "disabled-charging"
                }
                "disabled" -> {
                    InCarStorage.saveScreenTimeout(true, false, incar)
                    pref.value = "disabled"
                }
                else -> {
                    InCarStorage.saveScreenTimeout(false, false, incar)
                    pref.value = "enabled"
                }
            }
            false
        }
    }

    private fun initActivityRecognition() {
        val pref = findPreference(InCarSettings.ACTIVITY_RECOGNITION)
        val handler = Handler()

        Thread(Runnable {
            val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity!!)
            val summary = renderPlayServiceStatus(status)
            handler.post { updateActivityRecognition(status, summary, pref) }
        }).start()

    }

    private fun updateActivityRecognition(status: Int, summary: String,
                                          pref: Preference) {
        pref.summary = summary
        if (status != ConnectionResult.SUCCESS) {
            pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                val d = GoogleApiAvailability.getInstance()
                        .getErrorDialog(activity, status, PS_DIALOG_REQUEST_CODE)
                d.show()
                false
            }
        } else {
            pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val `val` = newValue as Boolean
                if (`val`) {
                    ActivityRecognitionClientService.startService(activity!!)
                } else {
                    ActivityRecognitionClientService.stopService(activity!!)
                }
                true
            }
        }

    }

    private fun renderPlayServiceStatus(errorCode: Int): String {
        if (errorCode == ConnectionResult.SUCCESS) {
            return getString(R.string.gms_success)
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
                InCarSettings.HEADSET_REQUIRED,
                InCarSettings.POWER_REQUIRED,
                InCarSettings.CAR_DOCK_REQUIRED,

                InCarSettings.POWER_BT_ENABLE,
                InCarSettings.POWER_BT_DISABLE
        )
    }
}