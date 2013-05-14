package com.anod.car.home.prefs;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.preference.Preference;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.Utils;

public class ConfigurationInCarVolume extends ConfigurationActivity implements OnCheckedChangeListener {
	
	@Override
	protected boolean isAppWidgetIdRequired() {
		return false;
	}
	
	@Override
	protected int getXmlResource() {
		return R.xml.preference_incar_volume;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		
		if (Utils.IS_ICS_OR_GREATER) {
			initGlobalSwtich();
		}
	}

	@SuppressLint("NewApi")
	private void initGlobalSwtich() {
		ActionBar actionbar = getActionBar();
		Switch actionBarSwitch = new Switch(this);
		actionBarSwitch.setOnCheckedChangeListener(this);
		actionBarSwitch.setChecked(PreferencesStorage.isAdjustVolumeLevel(this));
		
		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
						| Gravity.RIGHT));

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		PreferencesStorage.setAdjustVolumeLevel(this, isChecked);
	}

}
