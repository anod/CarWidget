package com.anod.car.home.prefs;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.colorpicker.ColorPickerSwatch;
import com.anod.car.home.R;
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences.WidgetEditor;
import com.anod.car.home.prefs.views.SeekBarDialogPreference;

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
        Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);

        final WidgetSharedPreferences sharedPrefs = new WidgetSharedPreferences(mContext);
        sharedPrefs.setAppWidgetId(mAppWidgetId);

        initIcon(prefs, sharedPrefs);
        initFont(prefs, sharedPrefs);

        ListPreference rotatePref = (ListPreference) initWidgetPref(
                PreferencesStorage.ICONS_ROTATE);
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

        initWidgetPrefCheckBox(PreferencesStorage.TITLES_HIDE, prefs.isTitlesHide());
        initWidgetPrefCheckBox(PreferencesStorage.TRANSPARENT_BTN_SETTINGS,
                prefs.isSettingsTransparent());
        initWidgetPrefCheckBox(PreferencesStorage.TRANSPARENT_BTN_INCAR,
                prefs.isIncarTransparent());

    }

    private void updateRotateSummary(ListPreference rotatePref, String value) {
        for (int i = 0; i < mIconRotateValues.length; i++) {
            if (value.equals(mIconRotateValues[i])) {
                rotatePref.setSummary(mIconRotateTitles[i]);
                break;
            }
        }
    }

    private void initIcon(final Main prefs, final WidgetSharedPreferences sharedPrefs) {
        Preference icnColor = (Preference) initWidgetPref(PreferencesStorage.ICONS_COLOR);
        icnColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Integer icnTintColor = prefs.getIconsColor();
                int value = (icnTintColor != null) ? icnTintColor : Color.WHITE;
                final CarHomeColorPickerDialog d = CarHomeColorPickerDialog
                        .newInstance(value, false, mContext);
                d.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        final WidgetEditor edit = sharedPrefs.edit();
                        edit.putInt(PreferencesStorage.ICONS_COLOR, color);
                        edit.commit();
                    }
                });
                d.show(getFragmentManager(), "icnColor");
                return false;

            }
        });

    }

    private void initFont(final Main prefs, final WidgetSharedPreferences sharedPrefs) {
        SeekBarDialogPreference sbPref = (SeekBarDialogPreference) initWidgetPref(PreferencesStorage.FONT_SIZE);
        int fontSize = prefs.getFontSize();
        if (fontSize != Main.FONT_SIZE_UNDEFINED) {
            sbPref.setValue(fontSize);
        } else {
            float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
            float size = 18 * scaledDensity;
            sbPref.setValue((int) size);
        }

        Preference fontColor = initWidgetPref(PreferencesStorage.FONT_COLOR);
        fontColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int value = prefs.getFontColor();
                final CarHomeColorPickerDialog d = CarHomeColorPickerDialog
                        .newInstance(value, true, mContext);
                d.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        final WidgetEditor edit = sharedPrefs.edit();
                        edit.putInt(PreferencesStorage.FONT_COLOR, color);
                        edit.commit();
                    }
                });
                d.show(getFragmentManager(), "fontColor");
                return false;

            }
        });
    }


}
