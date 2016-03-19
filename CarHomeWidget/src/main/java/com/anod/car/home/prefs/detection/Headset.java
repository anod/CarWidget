package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.InCarStorage;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

/**
 * @author alex
 * @date 1/15/14
 */
public class Headset extends Detection {

    @Override
    public boolean isActive() {
        return mPrefs.isHeadsetRequired();
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_action_headphones;
    }

    @Override
    public int getShortTitleRes() {
        return R.string.pref_headset_connected_title;
    }

    @Override
    public int getSummaryRes() {
        return R.string.pref_headset_connected_summary;
    }

    @Override
    public void onClick() {
        mPrefs.setHeadsetRequired(!mPrefs.isHeadsetRequired());
        InCarStorage.saveInCar(mContext, mPrefs);
    }
}
