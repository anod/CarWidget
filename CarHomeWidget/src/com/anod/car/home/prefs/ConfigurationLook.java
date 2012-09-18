package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;

import com.anod.car.home.Launcher;
import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.views.SeekBarPreference;

public class ConfigurationLook extends ConfigurationActivity {
	private static final String SKIN_PREVIEW = "skin-preview";

	public static final String CATEGORY_TRANSPARENT = "transparent-category";

	private boolean mFreeVersion;

	
	@Override
	protected int getTitleResource() {
		return R.string.pref_look_and_feel_title;
	}
	
	@Override
	protected int getXmlResource() {
		return R.xml.preference_look;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		mFreeVersion = Launcher.isFreeVersion(this.getPackageName());

		Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);

		initSkinPreview();
		initBackground(prefs);
		initIcon(prefs);
		initFont(prefs);
		initTransparent(mFreeVersion, prefs);
	}

	private void initSkinPreview() {
		Intent intent = new Intent(this, SkinPreviewActivity.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		Preference pref = (Preference) findPreference(SKIN_PREVIEW);
		pref.setIntent(intent);
	}

	private void initTransparent(boolean isFree, final Main prefs) {
		CheckBoxPreference setTrans = (CheckBoxPreference) findPreference(PreferencesStorage.TRANSPARENT_BTN_SETTINGS);
		String key = PreferencesStorage.getName(PreferencesStorage.TRANSPARENT_BTN_SETTINGS, mAppWidgetId);
		setTrans.setKey(key);
		setTrans.setChecked(prefs.isSettingsTransparent());

		CheckBoxPreference incarTrans = (CheckBoxPreference) findPreference(PreferencesStorage.TRANSPARENT_BTN_INCAR);

		if (isFree) {
			PreferenceCategory transCat = (PreferenceCategory) findPreference(CATEGORY_TRANSPARENT);
			transCat.removePreference(incarTrans);
		} else {
			key = PreferencesStorage.getName(PreferencesStorage.TRANSPARENT_BTN_INCAR, mAppWidgetId);
			incarTrans.setKey(key);
			incarTrans.setChecked(prefs.isIncarTransparent());
		}
	}

	private void initBackground(final Main prefs) {
		Preference bgColor = (Preference) findPreference(PreferencesStorage.BG_COLOR);
		bgColor.setKey(PreferencesStorage.getName(PreferencesStorage.BG_COLOR, mAppWidgetId));

		bgColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				int value = prefs.getBackgroundColor();
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.BG_COLOR, mAppWidgetId);
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

	private void initIcon(final Main prefs) {
		CheckBoxPreference icnMono = (CheckBoxPreference) findPreference(PreferencesStorage.ICONS_MONO);
		String key = PreferencesStorage.getName(PreferencesStorage.ICONS_MONO, mAppWidgetId);
		icnMono.setKey(key);
		icnMono.setChecked(prefs.isIconsMono());

		Preference icnColor = (Preference) findPreference(PreferencesStorage.ICONS_COLOR);
		icnColor.setKey(PreferencesStorage.getName(PreferencesStorage.ICONS_COLOR, mAppWidgetId));
		icnColor.setDependency(key);
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

		ListPreference icnScale = (ListPreference) findPreference(PreferencesStorage.ICONS_SCALE);
		icnScale.setKey(PreferencesStorage.getName(PreferencesStorage.ICONS_SCALE, mAppWidgetId));
		icnScale.setValue(prefs.getIconsScale());
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
