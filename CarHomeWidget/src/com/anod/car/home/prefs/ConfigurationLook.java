package com.anod.car.home.prefs;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.views.SeekBarPreference;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

public class ConfigurationLook extends ConfigurationActivity {

	public static final String CATEGORY_TRANSPARENT = "transparent-category";

	@Override
	protected int getXmlResource() {
		return R.xml.preference_look;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);

		initIcon(prefs);
		initFont(prefs);
		initTransparent(prefs);
	}

	private void initTransparent(final Main prefs) {
		CheckBoxPreference setTrans = (CheckBoxPreference) findPreference(PreferencesStorage.TRANSPARENT_BTN_SETTINGS);
		String key = PreferencesStorage.getName(PreferencesStorage.TRANSPARENT_BTN_SETTINGS, mAppWidgetId);
		setTrans.setKey(key);
		setTrans.setChecked(prefs.isSettingsTransparent());

		CheckBoxPreference incarTrans = (CheckBoxPreference) findPreference(PreferencesStorage.TRANSPARENT_BTN_INCAR);

		key = PreferencesStorage.getName(PreferencesStorage.TRANSPARENT_BTN_INCAR, mAppWidgetId);
		incarTrans.setKey(key);
		incarTrans.setChecked(prefs.isIncarTransparent());
	}


	private void initIcon(final Main prefs) {
		Preference icnColor = (Preference) findPreference(PreferencesStorage.ICONS_COLOR);
		icnColor.setKey(PreferencesStorage.getName(PreferencesStorage.ICONS_COLOR, mAppWidgetId));
		icnColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Integer icnTintColor = prefs.getIconsColor();
				int value = (icnTintColor != null) ? icnTintColor : Color.WHITE;
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.ICONS_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog) dialog).getColor();
						PreferencesStorage.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
		});

	}

	private void initFont(final Main prefs) {
		SeekBarPreference sbPref = (SeekBarPreference) findPreference(PreferencesStorage.FONT_SIZE);
		sbPref.setKey(PreferencesStorage.getName(PreferencesStorage.FONT_SIZE, mAppWidgetId));
		int fontSize = prefs.getFontSize();
		if (fontSize != PreferencesStorage.FONT_SIZE_UNDEFINED) {
			sbPref.setValue(fontSize);
		} else {
			float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
			float size = 18 * scaledDensity;
			sbPref.setValue((int) size);
		}

		Preference fontColor = (Preference) findPreference(PreferencesStorage.FONT_COLOR);
		fontColor.setKey(PreferencesStorage.getName(PreferencesStorage.FONT_COLOR, mAppWidgetId));
		fontColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				int value = prefs.getFontColor();
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.FONT_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog) dialog).getColor();
						PreferencesStorage.saveColor(mContext, prefName, color);
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
