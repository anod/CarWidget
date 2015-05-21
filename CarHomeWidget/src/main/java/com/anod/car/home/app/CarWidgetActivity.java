package com.anod.car.home.app;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.prefs.preferences.AppTheme;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * @author alex
 * @date 11/20/13
 */
abstract public class CarWidgetActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int theme = getApp().getThemeIdx();
        int themeRes = (isTransparentAppTheme()) ? AppTheme.getTransparentResource(theme)
                : AppTheme.getMainResource(theme);
        setTheme(themeRes);

        super.onCreate(savedInstanceState);

        //
    }

    protected boolean isTransparentAppTheme() {
        return false;
    }

    public CarWidgetApplication getApp() {
        return CarWidgetApplication.get(this);
    }

}
