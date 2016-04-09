package com.anod.car.home.prefs.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;

/**
 * @author algavris
 * @date 19/03/2016.
 */
public class AppSettings extends ChangeableSharedPreferences {
    public static final String MUSIC_APP = "music-app";
    private static final String APP_THEME = "app_theme";

    public static AppSettings create(Context context) {
        return new AppSettings(context);
    }

    public AppSettings(Context context) {
        super(getSharedPreferences(context));
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setMusicApp(ComponentName musicApp) {
        putChange(MUSIC_APP, musicApp);
    }

    public ComponentName getMusicApp() {
        String musicAppString = mPrefs.getString(MUSIC_APP, null);

        if (musicAppString != null) {
            return ComponentName.unflattenFromString(musicAppString);
        }
        return null;
    }

    public int getTheme() {
        return mPrefs.getInt(APP_THEME, AppTheme.THEME_GRAY);
    }

    public void setAppTheme(int theme) {
        putChange(APP_THEME, theme);
    }

}
