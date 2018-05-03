package com.anod.car.home.prefs

import android.graphics.Color
import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference

import com.anod.car.home.R
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.prefs.views.SeekBarDialogPreference

import java.util.Locale

class ConfigurationLook : ConfigurationPreferenceFragment() {

    private val iconRotateValues: Array<String> by lazy { resources.getStringArray(R.array.icon_rotate_values) ?: emptyArray() }
    private val iconRotateTitles: Array<String> by lazy { resources.getStringArray(R.array.icon_rotate_titles) ?: emptyArray() }

    override val xmlResource: Int
        get() = R.xml.preference_look

    override val sharedPreferencesName: String
        get() = String.format(Locale.US, WidgetStorage.PREF_NAME, appWidgetId)

    override fun onCreateImpl(savedInstanceState: Bundle?) {
        val prefs = WidgetStorage.load(activity, appWidgetId)

        initIcon(prefs)
        initFont(prefs)

        val rotatePref = findPreference(WidgetSettings.ICONS_ROTATE) as ListPreference
        rotatePref.value = prefs.iconsRotate.name

        updateRotateSummary(rotatePref, prefs.iconsRotate.name)
        rotatePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            updateRotateSummary(preference as ListPreference, o as String)
            true
        }

        initWidgetPrefCheckBox(WidgetSettings.TITLES_HIDE, prefs.isTitlesHide)
        initWidgetPrefCheckBox(WidgetSettings.TRANSPARENT_BTN_SETTINGS,
                prefs.isSettingsTransparent)
        initWidgetPrefCheckBox(WidgetSettings.TRANSPARENT_BTN_INCAR,
                prefs.isIncarTransparent)

    }

    private fun initWidgetPrefCheckBox(key: String, checked: Boolean) {
        val pref = findPreference(key) as CheckBoxPreference
        pref.isChecked = checked
    }

    private fun updateRotateSummary(rotatePref: ListPreference, value: String) {
        for (i in iconRotateValues.indices) {
            if (value == iconRotateValues[i]) {
                rotatePref.summary = iconRotateTitles[i]
                break
            }
        }
    }

    private fun initIcon(prefs: WidgetSettings) {
        val icnColor = findPreference(WidgetSettings.ICONS_COLOR)
        icnColor.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val icnTintColor = prefs.iconsColor
            val value = icnTintColor ?: Color.WHITE
            val d = CarHomeColorPickerDialog
                    .newInstance(value, false, activity!!)
            d.setOnColorSelectedListener { color ->
                prefs.iconsColor = color
                prefs.apply()
            }
            d.show(fragmentManager!!, "icnColor")
            false
        }

    }

    private fun initFont(prefs: WidgetSettings) {
        val sbPref = findPreference(WidgetSettings.FONT_SIZE) as SeekBarDialogPreference
        val fontSize = prefs.fontSize
        if (fontSize != WidgetSettings.FONT_SIZE_UNDEFINED) {
            sbPref.value = fontSize
        } else {
            val scaledDensity = resources.displayMetrics.scaledDensity
            val size = 18 * scaledDensity
            sbPref.value = size.toInt()
        }

        val fontColor = findPreference(WidgetSettings.FONT_COLOR)
        fontColor.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val value = prefs.fontColor
            val d = CarHomeColorPickerDialog
                    .newInstance(value, true, activity!!)
            d.setOnColorSelectedListener { color ->
                prefs.fontColor = color
                prefs.apply()
            }
            d.show(fragmentManager!!, "fontColor")
            false
        }
    }


}
