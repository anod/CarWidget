package com.anod.car.home.prefs;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences.WidgetEditor;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.views.SeekBarPreference;

public class ConfigurationLook extends ConfigurationActivity {

	public static final String CATEGORY_TRANSPARENT = "transparent-category";
	private String[] mIconRotateValues;
	private String[] mIconRotateTitles;

	@Override
	protected int getXmlResource() {
		return R.xml.preference_look;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);

		final WidgetSharedPreferences sharedPrefs = new WidgetSharedPreferences(mContext);
		sharedPrefs.setAppWidgetId(mAppWidgetId);
		
		initIcon(prefs, sharedPrefs);
		initFont(prefs, sharedPrefs);
		
		ListPreference rotatePref = (ListPreference)initWidgetPref(PreferencesStorage.ICONS_ROTATE);
		rotatePref.setValue(prefs.getIconsRotate().name());

		Resources r = getResources();
		mIconRotateValues = r.getStringArray(R.array.icon_rotate_values);
		mIconRotateTitles = r.getStringArray(R.array.icon_rotate_titles);
		updateRotateSummary(rotatePref, prefs.getIconsRotate().name());
		rotatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object o) {
				updateRotateSummary((ListPreference)preference, (String)o);
				return true;
			}
		});

		initWidgetPrefCheckBox(PreferencesStorage.TITLES_HIDE, prefs.isTitlesHide());
		initWidgetPrefCheckBox(PreferencesStorage.TRANSPARENT_BTN_SETTINGS, prefs.isSettingsTransparent());
		initWidgetPrefCheckBox(PreferencesStorage.TRANSPARENT_BTN_INCAR, prefs.isIncarTransparent());

	}

	private void updateRotateSummary(ListPreference rotatePref, String value) {
		for(int i = 0; i<mIconRotateValues.length; i++) {
			if (value.equals(mIconRotateValues[i])) {
				rotatePref.setSummary(mIconRotateTitles[i]);
				break;
			}
		}
	}

	private void initIcon(final Main prefs, final WidgetSharedPreferences sharedPrefs) {
		Preference icnColor = (Preference) initWidgetPref(PreferencesStorage.ICONS_COLOR);
		icnColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Integer icnTintColor = prefs.getIconsColor();
				int value = (icnTintColor != null) ? icnTintColor : Color.WHITE;
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int color = ((CarHomeColorPickerDialog) dialog).getColor();
						final WidgetEditor edit = sharedPrefs.edit();
						edit.putInt(PreferencesStorage.ICONS_COLOR, color);
						edit.commit();
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
		});

	}

	private void initFont(final Main prefs, final WidgetSharedPreferences sharedPrefs) {
		SeekBarPreference sbPref = (SeekBarPreference) initWidgetPref(PreferencesStorage.FONT_SIZE);
		int fontSize = prefs.getFontSize();
		if (fontSize != Main.FONT_SIZE_UNDEFINED) {
			sbPref.setValue(fontSize);
		} else {
			float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
			float size = 18 * scaledDensity;
			sbPref.setValue((int) size);
		}

		Preference fontColor = (Preference) initWidgetPref(PreferencesStorage.FONT_COLOR);
		fontColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				int value = prefs.getFontColor();
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int color = ((CarHomeColorPickerDialog) dialog).getColor();
						final WidgetEditor edit = sharedPrefs.edit();
						edit.putInt(PreferencesStorage.FONT_COLOR, color);
						edit.commit();
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.setAlphaSliderVisible(true);
				d.show();
				return false;

			}
		});
	}

}