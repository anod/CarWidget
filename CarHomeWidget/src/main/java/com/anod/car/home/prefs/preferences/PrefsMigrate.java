package com.anod.car.home.prefs.preferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anod.car.home.prefs.model.AppStorage;
import com.anod.car.home.prefs.model.InCarSettings;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.utils.AppLog;

import java.io.File;
import java.util.Locale;

/**
 * @author algavris
 * @date 19/03/2016.
 */
public class PrefsMigrate {

    private static final Object sLock = new Object();
    private static boolean sMigrated;
    private static final String INCAR_SHARED_PREFS_TPL = "/shared_prefs/%s.xml";

    public static InCarSettings migrate(Context context) {
        synchronized (sLock) {
            SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            InCarSettings prefs = loadInCar(defaultPrefs, context);
            prefs.apply();

            ComponentName musicApp = AppStorage.getMusicApp(defaultPrefs);
            AppStorage.saveMusicApp(context, musicApp, false);

            sMigrated = true;
            return prefs;
        }
    }

    public static boolean required(Context context) {
        synchronized (sLock) {
            if (sMigrated) {
                return false;
            }

            String filePath = context.getFilesDir().getParent() +
                    String.format(Locale.US, INCAR_SHARED_PREFS_TPL, InCarStorage.PREF_NAME);
            AppLog.d(filePath);
            File file = new File(filePath);
            return !file.exists();
        }
    }

    public static Main migrate(Context context, int appWidgetId) {
        synchronized (sLock) {
            Main prefs = WidgetMigrateStorage.loadMain(context, appWidgetId);
            //WidgetStorage.save(context, prefs, appWidgetId);
            return prefs;
        }
    }

    static InCarSettings loadInCar(SharedPreferences defaultPrefs, Context context) {

        SharedPreferences destPrefs = InCarStorage.getSharedPreferences(context);
        InCarSettings prefs = new InCarSettings(destPrefs);

        // TODO:
        return prefs;
    }
}
