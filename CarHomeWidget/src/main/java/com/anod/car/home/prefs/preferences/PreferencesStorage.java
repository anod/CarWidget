package com.anod.car.home.prefs.preferences;

import com.anod.car.home.R;
import com.anod.car.home.incar.ScreenOrientation;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences.WidgetEditor;
import com.anod.car.home.utils.BitmapTransform.RotateDirection;
import com.anod.car.home.utils.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class PreferencesStorage {

    public static final String CMP_NUMBER = "cmp-number-%d";

    public static final int LAUNCH_COMPONENT_NUMBER_MAX = 10;

    private static final int LAUNCH_COMPONENT_NUMBER_DEFAULT = 6;

    private static final String LAUNCH_COMPONENT = "launch-component-%d";

    public static final String SKIN = "skin-%d";

    public static final String BG_COLOR = "bg-color-%d";

    public static final String BUTTON_COLOR = "button-color-%d";

    public static final String ICONS_MONO = "icons-mono-%d";

    public static final String ICONS_COLOR = "icons-color-%d";

    public static final String ICONS_SCALE = "icons-scale-%d";

    public static final String FONT_SIZE = "font-size-%d";

    public static final String FONT_COLOR = "font-color-%d";

    public static final String FIRST_TIME = "first-time-%d";

    public static final String TRANSPARENT_BTN_SETTINGS = "transparent-btn-settings-%d";

    public static final String TRANSPARENT_BTN_INCAR = "transparent-btn-incar-%d";

    public static final String KEEP_ORDER = "keep-order-%d";

    private static final String ICONS_THEME = "icons-theme-%d";

    public static final String ICONS_ROTATE = "icons-rotate-%d";

    public static final String TITLES_HIDE = "titles-hide-%d";

    public static final String WIDGET_BUTTON_1 = "widget-button-1-%d";

    public static final String WIDGET_BUTTON_2 = "widget-button-2-%d";

    private static final String[] sAppWidgetPrefs = {
            SKIN,
            BG_COLOR,
            BUTTON_COLOR,
            ICONS_MONO,
            ICONS_COLOR,
            ICONS_SCALE,
            FONT_SIZE,
            FONT_COLOR,
            FIRST_TIME,
            TRANSPARENT_BTN_SETTINGS,
            TRANSPARENT_BTN_INCAR,
            KEEP_ORDER,
            ICONS_THEME,
            ICONS_ROTATE,
            TITLES_HIDE,
            WIDGET_BUTTON_1,
            WIDGET_BUTTON_2
    };

    private static final String ICONS_DEF_VALUE = "5";

    public static Main loadMain(Context context, int appWidgetId) {
        final WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        Resources res = context.getResources();

        Main p = new Main();
        String skinName = prefs.getString(SKIN, Main.SKIN_CARDS);
        p.setSkin(skinName);

        int defTileColor = res.getColor(R.color.w7_tale_default_background);
        int tileColor = prefs.getInt(BUTTON_COLOR, defTileColor);
        p.setTileColor(tileColor);

        p.setIconsScaleString(prefs.getString(ICONS_SCALE, ICONS_DEF_VALUE));
        p.setIconsMono(prefs.getBoolean(ICONS_MONO, false));
        p.setBackgroundColor(prefs.getInt(BG_COLOR, res.getColor(R.color.default_background)));
        p.setIconsColor(prefs.getColor(ICONS_COLOR));
        p.setFontColor(prefs.getInt(FONT_COLOR, res.getColor(R.color.default_font_color)));
        p.setFontSize(prefs.getInt(FONT_SIZE, Main.FONT_SIZE_UNDEFINED));
        p.setSettingsTransparent(prefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false));
        p.setIncarTransparent(prefs.getBoolean(TRANSPARENT_BTN_INCAR, false));
        p.setIconsTheme(prefs.getString(ICONS_THEME, null));

        p.setIconsRotate(RotateDirection
                .valueOf(prefs.getString(ICONS_ROTATE, RotateDirection.NONE.name())));
        p.setTitlesHide(prefs.getBoolean(TITLES_HIDE, false));

        p.setWidgetButton1(prefs.getInt(WIDGET_BUTTON_1, Main.WIDGET_BUTTON_INCAR));
        p.setWidgetButton2(prefs.getInt(WIDGET_BUTTON_2, Main.WIDGET_BUTTON_SETTINGS));

        return p;
    }

    public static void saveMain(Context context, Main prefs, int appWidgetId) {
        final WidgetSharedPreferences p = new WidgetSharedPreferences(context);
        p.setAppWidgetId(appWidgetId);

        WidgetEditor editor = p.edit();

        editor.putString(SKIN, prefs.getSkin());

        Integer defTileColor = prefs.getTileColor();
        if (defTileColor != null) {
            editor.putInt(BUTTON_COLOR, defTileColor);
        }
        editor.putString(ICONS_SCALE, prefs.getIconsScale());
        editor.putBoolean(ICONS_MONO, prefs.isIconsMono());
        editor.putInt(BG_COLOR, prefs.getBackgroundColor());
        Integer iconsColor = prefs.getIconsColor();
        if (iconsColor != null) {
            editor.putInt(ICONS_COLOR, iconsColor);
        }

        editor.putInt(FONT_COLOR, prefs.getFontColor());
        editor.putInt(FONT_SIZE, prefs.getFontSize());

        editor.putBoolean(TRANSPARENT_BTN_SETTINGS, prefs.isSettingsTransparent());
        editor.putBoolean(TRANSPARENT_BTN_INCAR, prefs.isIncarTransparent());

        editor.putStringOrRemove(ICONS_THEME, prefs.getIconsTheme());

        editor.putString(ICONS_ROTATE, prefs.getIconsRotate().name());
        editor.putBoolean(TITLES_HIDE, prefs.isTitlesHide());

        editor.putInt(WIDGET_BUTTON_1, prefs.getWidgetButton1());
        editor.putInt(WIDGET_BUTTON_2, prefs.getWidgetButton2());

        editor.commit();

    }

    public static String getLaunchComponentKey(int id) {
        return String.format(Locale.US, LAUNCH_COMPONENT, id) + "-%d";
    }

    public static String getLaunchComponentName(int id, int aAppWidgetId) {
        return String.format(getLaunchComponentKey(id), aAppWidgetId);
    }

    public static ArrayList<Long> getLauncherComponents(Context context, int appWidgetId,
            int count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<Long> ids = new ArrayList<Long>(count);
        for (int i = 0; i < count; i++) {
            String key = PreferencesStorage.getLaunchComponentName(i, appWidgetId);
            long id = prefs.getLong(key, ShortcutInfo.NO_ID);
            ids.add(i, id);
        }
        return ids;
    }

    public static int getLaunchComponentNumber(Context context, int appWidgetId) {
        WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        int num = prefs
                .getInt(CMP_NUMBER, prefs.getInt("cmp-number", LAUNCH_COMPONENT_NUMBER_DEFAULT));
        return (num == 0) ? LAUNCH_COMPONENT_NUMBER_DEFAULT : num;
    }

    public static void saveLaunchComponentNumber(Integer count, Context context, int appWidgetId) {
        WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        Editor edit = prefs.edit();
        edit.putInt(CMP_NUMBER, count);
        edit.commit();
    }

    public static boolean isFirstTime(Context context, int appWidgetId) {
        WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        return prefs.getBoolean(FIRST_TIME, true);
    }

    public static void setFirstTime(boolean value, Context context, int appWidgetId) {
        WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        WidgetEditor editor = prefs.edit();
        editor.putBoolean(FIRST_TIME, value);
        editor.commit();
    }

    public static void saveShortcut(Context context, long shortcutId, int cellId, int appWidgetId) {
        String key = getLaunchComponentName(cellId, appWidgetId);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        saveShortcutId(context, preferences, shortcutId, key);
    }

    static void saveShortcutId(Context context, SharedPreferences preferences, long shortcutId, String key) {
        long curShortcutId = preferences.getLong(key, ShortcutInfo.NO_ID);
        if (curShortcutId != ShortcutInfo.NO_ID) {
            ShortcutModel model = new ShortcutModel(context);
            model.deleteItemFromDatabase(curShortcutId);
        }
        Editor editor = preferences.edit();
        editor.putLong(key, shortcutId);
        editor.commit();
    }

    public static void dropWidgetSettings(Context context, int[] appWidgetIds) {
        ShortcutModel model = new ShortcutModel(context);
        WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        for (int appWidgetId : appWidgetIds) {
            prefs.setAppWidgetId(appWidgetId);
            WidgetEditor edit = prefs.edit();
            for (int i = 0; i < sAppWidgetPrefs.length; i++) {
                edit.remove(sAppWidgetPrefs[i]);
            }

            for (int i = 0; i < LAUNCH_COMPONENT_NUMBER_MAX; i++) {
                String key = getLaunchComponentKey(i);
                long curShortcutId = prefs.getLong(key, ShortcutInfo.NO_ID);
                if (curShortcutId != ShortcutInfo.NO_ID) {
                    model.deleteItemFromDatabase(curShortcutId);
                }
                edit.remove(key);
            }
            edit.commit();
        }
    }

    public static void dropShortcutPreference(int cellId, int appWidgetId, Context context) {
        String key = getLaunchComponentName(cellId, appWidgetId);
        remove(context, key);
    }

    private static void remove(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = prefs.edit();
        edit.remove(key);
        edit.commit();
    }

}
