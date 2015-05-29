package com.anod.car.home.app;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.prefs.preferences.AppTheme;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

/**
 * @author alex
 * @date 11/20/13
 */
abstract public class CarWidgetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int theme = getApp().getThemeIdx();
        setTheme(getAppThemeRes(theme));
        super.onCreate(savedInstanceState);

    }

    protected int getAppThemeRes(int theme) {
        return (isTransparentAppTheme()) ? AppTheme.getTransparentResource(theme)
                : AppTheme.getMainResource(theme);
    }

    protected boolean isTransparentAppTheme() {
        return false;
    }

    public CarWidgetApplication getApp() {
        return CarWidgetApplication.get(this);
    }

}
