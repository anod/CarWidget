package com.anod.car.home.prefs;

import com.anod.car.home.R;
import com.anod.car.home.drawer.NavigationList;
import com.anod.car.home.prefs.preferences.InCarStorage;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfigurationInCarVolume extends ConfigurationPreferenceFragment
        implements OnCheckedChangeListener {

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
        InCarStorage.setAdjustVolumeLevel(mContext, isChecked);
    }

}
