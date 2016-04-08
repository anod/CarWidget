package com.anod.car.home.prefs;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.colorpicker.ColorPickerSwatch;
import com.anod.car.home.R;
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.WidgetStorage;
import com.anod.car.home.prefs.views.SeekBarDialogPreference;

import java.util.Locale;

public class ConfigurationLook extends ConfigurationPreferenceFragment {

    public static final String CATEGORY_TRANSPARENT = "transparent-category";

    private String[] mIconRotateValues;

    private String[] mIconRotateTitles;

    @Override
    protected int getXmlResource() {
        return R.xml.preference_look;
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        Main prefs = WidgetStorage.load(getActivity(), mAppWidgetId);

        final SharedPreferences sharedPrefs = WidgetStorage.getSharedPreferences(getActivity(), mAppWidgetId);

        initIcon(prefs, sharedPrefs);
        initFont(prefs, sharedPrefs);

        ListPreference rotatePref = (ListPreference) findPreference(WidgetStorage.ICONS_ROTATE);
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

        initWidgetPrefCheckBox(WidgetStorage.TITLES_HIDE, prefs.isTitlesHide());
        initWidgetPrefCheckBox(WidgetStorage.TRANSPARENT_BTN_SETTINGS,
                prefs.isSettingsTransparent());
        initWidgetPrefCheckBox(WidgetStorage.TRANSPARENT_BTN_INCAR,
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

    private void initIcon(final Main prefs, final SharedPreferences sharedPrefs) {
        Preference icnColor = findPreference(WidgetStorage.ICONS_COLOR);
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
                        final SharedPreferences.Editor edit = sharedPrefs.edit();
                        edit.putInt(WidgetStorage.ICONS_COLOR, color);
                        edit.commit();
                    }
                });
                d.show(getFragmentManager(), "icnColor");
                return false;
            }
        });

    }

    private void initFont(final Main prefs, final SharedPreferences sharedPrefs) {
        SeekBarDialogPreference sbPref = (SeekBarDialogPreference) findPreference(WidgetStorage.FONT_SIZE);
        int fontSize = prefs.getFontSize();
        if (fontSize != Main.FONT_SIZE_UNDEFINED) {
            sbPref.setValue(fontSize);
        } else {
            float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            float size = 18 * scaledDensity;
            sbPref.setValue((int) size);
        }

        Preference fontColor = findPreference(WidgetStorage.FONT_COLOR);
        fontColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int value = prefs.getFontColor();
                final CarHomeColorPickerDialog d = CarHomeColorPickerDialog
                        .newInstance(value, true, getActivity());
                d.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        final SharedPreferences.Editor edit = sharedPrefs.edit();
                        edit.putInt(WidgetStorage.FONT_COLOR, color);
                        edit.commit();
                    }
                });
                d.show(getFragmentManager(), "fontColor");
                return false;

            }
        });
    }


}
