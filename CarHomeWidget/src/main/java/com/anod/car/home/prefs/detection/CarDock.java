package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

/**
 * @author alex
 * @date 1/15/14
 */
public class CarDock extends Detection {

    @Override
    public boolean isActive() {
        return mPrefs.isCarDockRequired();
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_action_wheel;
    }

    @Override
    public int getShortTitleRes() {
        return R.string.car_dock;
    }

    @Override
    public int getSummaryRes() {
        return R.string.car_dock_summary;
    }

    @Override
    public void onClick() {
        mPrefs.setCarDockRequired(!mPrefs.isCarDockRequired());
        PreferencesStorage.saveInCar(mContext, mPrefs);
    }
}
