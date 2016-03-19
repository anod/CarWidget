package com.anod.car.home.prefs.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.Locale;

/**
 * @author algavris
 * @date 19/03/2016.
 */
public class InCarMigrate {

    private static final Object sLock = new Object();
    private static boolean sMigrated;
    private static final String FILE_PATH_TPL = "/data/data/%s/shared_prefs/%s.xml";

    public static InCar migrate(Context context) {
        synchronized (sLock) {
            SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            InCar prefs = InCarStorage.loadInCar(defaultPrefs);
            InCarStorage.saveInCar(context, prefs);
            sMigrated = true;
            return prefs;
        }
    }

    public static boolean required(Context context) {
        synchronized (sLock) {
            if (sMigrated) {
                return false;
            }
            File file = new File(String.format(Locale.US, FILE_PATH_TPL, context.getPackageName(), InCarStorage.PREF_NAME));
            return !file.exists();
        }
    }
}
