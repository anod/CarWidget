package com.anod.car.home.prefs;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.anod.car.home.R;
import com.anod.car.home.drawer.NavigationList;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

public class ConfigurationInCarVolume extends ConfigurationPreferenceFragment implements OnCheckedChangeListener {
	
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

    @Override
    protected int getNavigationItem() {
        return NavigationList.ID_CAR_SETTINGS;
    }
}
