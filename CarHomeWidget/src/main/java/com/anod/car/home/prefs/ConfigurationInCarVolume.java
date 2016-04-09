package com.anod.car.home.prefs;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.anod.car.home.R;
import com.anod.car.home.prefs.model.InCarSettings;
import com.anod.car.home.prefs.model.InCarStorage;

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
    protected String getSharedPreferencesName() {
        return InCarStorage.PREF_NAME;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        InCarSettings prefs = InCarStorage.load(getActivity());
        prefs.setAdjustVolumeLevel(isChecked);
        prefs.apply();
    }

}
