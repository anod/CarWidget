package com.anod.car.home.prefs.preferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author algavris
 * @date 19/03/2016.
 */
public class AppStorage {
    public static final String MUSIC_APP = "music-app";
    static final String PREF_NAME = "app";

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveMusicApp(Context context, ComponentName musicApp, boolean delayed) {
        saveMusicApp(getSharedPreferences(context), musicApp, delayed);
    }

    public static ComponentName getMusicApp(Context context) {
        return getMusicApp(getSharedPreferences(context));
    }

    static void saveMusicApp(SharedPreferences prefs, ComponentName musicApp, boolean delayed) {
        SharedPreferences.Editor edit = prefs.edit();
        if (musicApp == null) {
            edit.remove(MUSIC_APP);
        } else {
            edit.putString(MUSIC_APP, musicApp.flattenToString());
        }
        if (delayed) {
            edit.apply();
        } else {
            edit.commit();
        }
    }

    static ComponentName getMusicApp(SharedPreferences prefs) {
        String musicAppString = prefs.getString(MUSIC_APP, null);

        if (musicAppString != null) {
            return ComponentName.unflattenFromString(musicAppString);
        }
        return null;
    }


}
