package com.anod.car.home.prefs.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.utils.AppLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class WidgetStorage {

    public static final int LAUNCH_COMPONENT_NUMBER_MAX = 10;
    private static final int LAUNCH_COMPONENT_NUMBER_DEFAULT = 6;

    public static final String CMP_NUMBER = "cmp-number";
    private static final String LAUNCH_COMPONENT = "launch-component-%d";

    public static final String PREF_NAME = "widget-%d";
    public static final String SHARED_PREFS_PATH = "/shared_prefs/%s.xml";

    public static SharedPreferences getSharedPreferences(Context context, int appWidgetId) {
        String prefName = String.format(Locale.US, PREF_NAME, appWidgetId);
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static WidgetSettings load(Context context, int appWidgetId) {
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        return new WidgetSettings(prefs, context.getResources());
    }

    public static String getLaunchComponentKey(int position) {
        return String.format(Locale.US, LAUNCH_COMPONENT, position);
    }

    public static ArrayList<Long> getLauncherComponents(Context context, int appWidgetId,
                                                        int count) {
        SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        ArrayList<Long> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String key = getLaunchComponentKey(i);
            long id = prefs.getLong(key, ShortcutInfo.NO_ID);
            ids.add(i, id);
        }
        return ids;
    }

    public static int getLaunchComponentNumber(Context context, int appWidgetId) {
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        int num = prefs
                .getInt(CMP_NUMBER, prefs.getInt("cmp-number", LAUNCH_COMPONENT_NUMBER_DEFAULT));
        return (num == 0) ? LAUNCH_COMPONENT_NUMBER_DEFAULT : num;
    }

    public static void saveLaunchComponentNumber(Integer count, Context context, int appWidgetId) {
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        Editor edit = prefs.edit();
        edit.putInt(CMP_NUMBER, count);
        edit.apply();
    }

    public static void saveShortcut(Context context, long shortcutId, int cellId, int appWidgetId) {
        String key = getLaunchComponentKey(cellId);
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        saveShortcutId(context, prefs, shortcutId, key);
    }

    static void saveShortcutId(Context context, SharedPreferences preferences, long shortcutId, String key) {
        long curShortcutId = preferences.getLong(key, ShortcutInfo.NO_ID);
        if (curShortcutId != ShortcutInfo.NO_ID) {
            ShortcutModel model = new ShortcutModel(context);
            model.deleteItemFromDatabase(curShortcutId);
        }
        Editor editor = preferences.edit();
        editor.putLong(key, shortcutId);
        editor.apply();
    }

    public static void dropWidgetSettings(Context context, int[] appWidgetIds) {
        ShortcutModel model = new ShortcutModel(context);
        for (int appWidgetId : appWidgetIds) {
            final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);

            for (int i = 0; i < LAUNCH_COMPONENT_NUMBER_MAX; i++) {
                String key = getLaunchComponentKey(i);
                long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
                if (curShortcutId != ShortcutInfo.NO_ID) {
                    model.deleteItemFromDatabase(curShortcutId);
                }
            }

            String prefName = String.format(Locale.US, PREF_NAME, appWidgetId);
            String filePath = context.getFilesDir().getParent() +
                    String.format(Locale.US, WidgetStorage.SHARED_PREFS_PATH, prefName);
            AppLog.d("Drop widget file: " + filePath);
            File file = new File(filePath);
            file.delete();
        }
    }

    public static void dropShortcutPreference(int cellId, int appWidgetId, Context context) {
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        Editor edit = prefs.edit();
        String key = getLaunchComponentKey(cellId);
        edit.remove(key);
        edit.apply();
    }
}