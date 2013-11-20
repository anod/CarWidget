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

public class ConfigurationInCarVolume extends ConfigurationFragment implements OnCheckedChangeListener {
	
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
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		PreferencesStorage.setAdjustVolumeLevel(mContext, isChecked);
	}

}
