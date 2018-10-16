package com.anod.car.home.prefs

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.android.colorpicker.ColorPickerSwatch

import com.anod.car.home.R
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog
import com.anod.car.home.prefs.model.WidgetInterface
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.prefs.views.SeekBarDialogPreference

import java.util.Locale

class ConfigurationLook : ConfigurationPreferenceFragment() {

    private val iconRotateValues: Array<String> by lazy { resources.getStringArray(R.array.icon_rotate_values) ?: emptyArray() }
    private val iconRotateTitles: Array<String> by lazy { resources.getStringArray(R.array.icon_rotate_titles) ?: emptyArray() }
    private val adaptiveIconTitles: Array<String> by lazy { resources.getStringArray(R.array.adaptive_icon_style_names) ?: emptyArray() }
    private val adaptiveIconValues: Array<String> by lazy { resources.getStringArray(R.array.adaptive_icon_style_paths_values) ?: emptyArray() }

    override val xmlResource: Int
        get() = R.xml.preference_look

    override val sharedPreferencesName: String
        get() = String.format(Locale.US, WidgetStorage.PREF_NAME, appWidgetId)

    override fun onCreateImpl(savedInstanceState: Bundle?) {
        val prefs = WidgetStorage.load(context!!, appWidgetId)

        initIcon(prefs)
        initFont(prefs)

        val rotatePref = findPreference(WidgetSettings.ICONS_ROTATE) as ListPreference
        rotatePref.value = prefs.iconsRotate.name
        updateRotateSummary(rotatePref, prefs.iconsRotate.name)
        rotatePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            updateRotateSummary(preference as ListPreference, o as String)
            true
        }

        val adaptiveIconPref = findPreference(WidgetSettings.ADAPTIVE_ICON_STYLE) as ListPreference
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adaptiveIconPref.value = prefs.adaptiveIconStyle
            adaptiveIconPref.summary = adaptiveIconTitles.elementAtOrNull(adaptiveIconValues.indexOf(prefs.adaptiveIconStyle))

            adaptiveIconPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
                preference.summary = adaptiveIconTitles.elementAtOrNull(adaptiveIconValues.indexOf(o))
                true
            }
        } else {
            (findPreference("look-more-category") as PreferenceCategory).removePreference(adaptiveIconPref)
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
            val d = CarHomeColorPickerDialog.newInstance(value, false, activity!!)
            d.listener = ColorPickerSwatch.OnColorSelectedListener { color ->
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
        if (fontSize != WidgetInterface.FONT_SIZE_UNDEFINED) {
            sbPref.value = fontSize
        } else {
            val scaledDensity = resources.displayMetrics.scaledDensity
            val size = 18 * scaledDensity
            sbPref.value = size.toInt()
        }

        val fontColor = findPreference(WidgetSettings.FONT_COLOR)
        fontColor.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val value = prefs.fontColor
            val d = CarHomeColorPickerDialog.newInstance(value, true, activity!!)
            d.listener = ColorPickerSwatch.OnColorSelectedListener{ color ->
                prefs.fontColor = color
                prefs.apply()
            }
            d.show(fragmentManager!!, "fontColor")
            false
        }
    }


}
