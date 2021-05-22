package com.anod.car.home.prefs

import android.graphics.Color
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.android.colorpicker.ColorPickerSwatch
import com.anod.car.home.R
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog
import info.anodsplace.carwidget.preferences.model.WidgetInterface
import info.anodsplace.carwidget.preferences.model.WidgetSettings
import info.anodsplace.carwidget.preferences.model.WidgetStorage
import com.anod.car.home.prefs.views.SeekBarDialogPreference
import java.util.*

class ConfigurationLook : ConfigurationPreferenceFragment() {

    private val iconRotateValues: Array<String> by lazy { resources.getStringArray(R.array.icon_rotate_values) }
    private val iconRotateTitles: Array<String> by lazy { resources.getStringArray(R.array.icon_rotate_titles) }
    private val adaptiveIconTitles: Array<String> by lazy { resources.getStringArray(R.array.adaptive_icon_style_names) }
    private val adaptiveIconValues: Array<String> by lazy { resources.getStringArray(R.array.adaptive_icon_style_paths_values) }

    override val xmlResource: Int
        get() = R.xml.preference_look

    override val sharedPreferencesName: String
        get() = String.format(Locale.US, WidgetStorage.PREF_NAME, appWidgetId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = WidgetStorage.load(requireContext(), appWidgetId)

        initIcon(prefs)
        initFont(prefs)

        val rotatePref = requirePreference(WidgetSettings.ICONS_ROTATE) as ListPreference
        rotatePref.value = prefs.iconsRotate.name
        updateRotateSummary(rotatePref, prefs.iconsRotate.name)
        rotatePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            updateRotateSummary(preference as ListPreference, o as String)
            true
        }

        val adaptiveIconPref = requirePreference(WidgetSettings.ADAPTIVE_ICON_STYLE) as ListPreference
        adaptiveIconPref.value = prefs.adaptiveIconStyle
        adaptiveIconPref.summary = adaptiveIconTitles.elementAtOrNull(adaptiveIconValues.indexOf(prefs.adaptiveIconStyle))

        adaptiveIconPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            preference.summary = adaptiveIconTitles.elementAtOrNull(adaptiveIconValues.indexOf(o))
            true
        }

        initWidgetPrefCheckBox(WidgetSettings.TITLES_HIDE, prefs.isTitlesHide)
        initWidgetPrefCheckBox(WidgetSettings.TRANSPARENT_BTN_SETTINGS, prefs.isSettingsTransparent)
        initWidgetPrefCheckBox(WidgetSettings.TRANSPARENT_BTN_INCAR, prefs.isIncarTransparent)
    }

    private fun initWidgetPrefCheckBox(key: String, checked: Boolean) {
        val pref = requirePreference(key) as CheckBoxPreference
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
        val icnColor: Preference = requirePreference(WidgetSettings.ICONS_COLOR)
        icnColor.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val icnTintColor = prefs.iconsColor
            val value = icnTintColor ?: Color.WHITE
            val d = CarHomeColorPickerDialog.newInstance(value, false, requireActivity())
            d.listener = ColorPickerSwatch.OnColorSelectedListener { color ->
                prefs.iconsColor = color
                prefs.apply()
            }
            d.show(parentFragmentManager, "icnColor")
            false
        }

    }

    private fun initFont(prefs: WidgetSettings) {
        val sbPref = requirePreference(WidgetSettings.FONT_SIZE) as SeekBarDialogPreference
        val fontSize = prefs.fontSize
        if (fontSize != WidgetInterface.FONT_SIZE_UNDEFINED) {
            sbPref.value = fontSize
        } else {
            val scaledDensity = resources.displayMetrics.scaledDensity
            val size = 18 * scaledDensity
            sbPref.value = size.toInt()
        }

        val fontColor: Preference = requirePreference(WidgetSettings.FONT_COLOR)
        fontColor.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val value = prefs.fontColor
            val d = CarHomeColorPickerDialog.newInstance(value, true, requireActivity())
            d.listener = ColorPickerSwatch.OnColorSelectedListener{ color ->
                prefs.fontColor = color
                prefs.apply()
            }
            d.show(parentFragmentManager, "fontColor")
            false
        }
    }
}
