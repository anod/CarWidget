package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.anod.car.home.model.ShortcutInfo;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author algavris
 * @date 19/03/2016.
 */
@SuppressLint("CommitPrefEdits")
public class InCarStorage {
    public static final int NOTIFICATION_COMPONENT_NUMBER = 3;
    private static final String MODE_FORCE_STATE = "mode-force-state";
    private static final String NOTIF_COMPONENT = "notif-component-%d";

    public static final String PREF_NAME = "incar";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static InCarSharedPreferences load(Context context) {
        if (PrefsMigrate.required(context)) {
            return PrefsMigrate.migrate(context);
        }

        return new InCarSharedPreferences(getSharedPreferences(context));
    }

    public static void saveScreenTimeout(boolean disabled, boolean disableCharging, InCarSharedPreferences prefs) {
        prefs.setDisableScreenTimeout(disabled);
        prefs.setDisableScreenTimeoutCharging(disableCharging);
        prefs.apply();
    }

    public static String getNotifComponentName(int position) {
        return String.format(Locale.US, NOTIF_COMPONENT, position);
    }

    public static void dropNotifShortcut(int position, Context context) {
        String key = getNotifComponentName(position);
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }

    public static ArrayList<Long> getNotifComponents(Context context) {
        return getNotifComponents(getSharedPreferences(context));
    }

    public static void saveNotifShortcut(Context context, long shortcutId, int position) {
        saveNotifShortcut(context, getSharedPreferences(context), shortcutId, position);
    }

    static void saveNotifShortcut(Context context, SharedPreferences prefs, long shortcutId, int position) {
        String key = getNotifComponentName(position);
        WidgetStorage.saveShortcutId(context, prefs, shortcutId, key);
    }

    static ArrayList<Long> getNotifComponents(SharedPreferences prefs) {
        ArrayList<Long> ids = new ArrayList<Long>(NOTIFICATION_COMPONENT_NUMBER);
        for (int i = 0; i < NOTIFICATION_COMPONENT_NUMBER; i++) {
            String key = getNotifComponentName(i);
            long id = prefs.getLong(key, ShortcutInfo.NO_ID);
            ids.add(i, id);
        }
        return ids;
    }

}
