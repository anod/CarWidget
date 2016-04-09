package com.anod.car.home.prefs;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.colorpicker.ColorPickerSwatch;
import com.anod.car.home.R;
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.model.WidgetSettings;
import com.anod.car.home.prefs.model.WidgetStorage;
import com.anod.car.home.prefs.views.SeekBarDialogPreference;

import java.util.Locale;

public class ConfigurationLook extends ConfigurationPreferenceFragment {

    private String[] mIconRotateValues;

    private String[] mIconRotateTitles;

    @Override
    protected int getXmlResource() {
        return R.xml.preference_look;
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        WidgetSettings prefs = WidgetStorage.load(getActivity(), mAppWidgetId);

        initIcon(prefs);
        initFont(prefs);

        ListPreference rotatePref = (ListPreference) findPreference(WidgetSettings.ICONS_ROTATE);
        rotatePref.setValue(prefs.getIconsRotate().name());

        Resources r = getResources();
        mIconRotateValues = r.getStringArray(R.array.icon_rotate_values);
        mIconRotateTitles = r.getStringArray(R.array.icon_rotate_titles);
        updateRotateSummary(rotatePref, prefs.getIconsRotate().name());
        rotatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                updateRotateSummary((ListPreference) preference, (String) o);
                return true;
            }
        });

        initWidgetPrefCheckBox(WidgetSettings.TITLES_HIDE, prefs.isTitlesHide());
        initWidgetPrefCheckBox(WidgetSettings.TRANSPARENT_BTN_SETTINGS,
                prefs.isSettingsTransparent());
        initWidgetPrefCheckBox(WidgetSettings.TRANSPARENT_BTN_INCAR,
                prefs.isIncarTransparent());

    }

    private void initWidgetPrefCheckBox(String key, boolean checked) {
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
        pref.setChecked(checked);
    }

    @Override
    protected String getSharedPreferencesName() {
        return String.format(Locale.US, WidgetStorage.PREF_NAME, mAppWidgetId);
    }

    private void updateRotateSummary(ListPreference rotatePref, String value) {
        for (int i = 0; i < mIconRotateValues.length; i++) {
            if (value.equals(mIconRotateValues[i])) {
                rotatePref.setSummary(mIconRotateTitles[i]);
                break;
            }
        }
    }

    private void initIcon(final WidgetSettings prefs) {
        Preference icnColor = findPreference(WidgetSettings.ICONS_COLOR);
        icnColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Integer icnTintColor = prefs.getIconsColor();
                int value = (icnTintColor != null) ? icnTintColor : Color.WHITE;
                final CarHomeColorPickerDialog d = CarHomeColorPickerDialog
                        .newInstance(value, false, getActivity());
                d.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        prefs.setIconsColor(color);
                        prefs.apply();
                    }
                });
                d.show(getFragmentManager(), "icnColor");
                return false;
            }
        });

    }

    private void initFont(final WidgetSettings prefs) {
        SeekBarDialogPreference sbPref = (SeekBarDialogPreference) findPreference(WidgetSettings.FONT_SIZE);
        int fontSize = prefs.getFontSize();
        if (fontSize != WidgetSettings.FONT_SIZE_UNDEFINED) {
            sbPref.setValue(fontSize);
        } else {
            float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            float size = 18 * scaledDensity;
            sbPref.setValue((int) size);
        }

        Preference fontColor = findPreference(WidgetSettings.FONT_COLOR);
        fontColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int value = prefs.getFontColor();
                final CarHomeColorPickerDialog d = CarHomeColorPickerDialog
                        .newInstance(value, true, getActivity());
                d.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        prefs.setFontColor(color);
                        prefs.apply();
                    }
                });
                d.show(getFragmentManager(), "fontColor");
                return false;

            }
        });
    }


}
