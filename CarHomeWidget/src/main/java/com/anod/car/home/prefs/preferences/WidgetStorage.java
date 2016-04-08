package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.utils.ApiCompat;
import com.anod.car.home.utils.BitmapTransform.RotateDirection;

import java.util.ArrayList;
import java.util.Locale;

@SuppressLint("CommitPrefEdits")
public class WidgetStorage {

    public static final int LAUNCH_COMPONENT_NUMBER_MAX = 10;
    private static final int LAUNCH_COMPONENT_NUMBER_DEFAULT = 6;


    public static final String CMP_NUMBER = "cmp-number";
    private static final String LAUNCH_COMPONENT = "launch-component-%d";

    private static final String SKIN = "skin";
    public static final String BG_COLOR = "bg-color";
    public static final String BUTTON_COLOR = "button-color";
    private static final String ICONS_MONO = "icons-mono";
    public static final String ICONS_COLOR = "icons-color";
    private static final String ICONS_SCALE = "icons-scale";
    public static final String FONT_SIZE = "font-size";
    public static final String FONT_COLOR = "font-color";
    private static final String FIRST_TIME = "first-time";
    public static final String TRANSPARENT_BTN_SETTINGS = "transparent-btn-settings";
    public static final String TRANSPARENT_BTN_INCAR = "transparent-btn-incar";
    private static final String KEEP_ORDER = "keep-order";
    private static final String ICONS_THEME = "icons-theme";
    public static final String ICONS_ROTATE = "icons-rotate";
    public static final String TITLES_HIDE = "titles-hide";
    private static final String WIDGET_BUTTON_1 = "widget-button-1";
    private static final String WIDGET_BUTTON_2 = "widget-button-2";

    private static final String ICONS_DEF_VALUE = "5";

    public static final String PREF_NAME = "widget-%d";

    public static SharedPreferences getSharedPreferences(Context context, int appWidgetId) {
        String prefName = String.format(Locale.US, PREF_NAME, appWidgetId);
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static Main load(Context context, int appWidgetId) {
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        Resources res = context.getResources();
        int defTileColor = ApiCompat.getColor(res, R.color.w7_tale_default_background);

        Main p = new Main();
        String skinName = prefs.getString(SKIN, Main.SKIN_CARDS);
        p.setSkin(skinName);

        p.setTileColor(prefs.getInt(BUTTON_COLOR, defTileColor));

        p.setIconsScaleString(prefs.getString(ICONS_SCALE, ICONS_DEF_VALUE));
        p.setIconsMono(prefs.getBoolean(ICONS_MONO, false));
        p.setBackgroundColor(prefs.getInt(BG_COLOR, ApiCompat.getColor(res, R.color.default_background)));
        p.setIconsColor(getColor(ICONS_COLOR, prefs));
        p.setFontColor(prefs.getInt(FONT_COLOR, ApiCompat.getColor(res, R.color.default_font_color)));
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

    public static void save(Context context, Main prefs, int appWidgetId) {
        final SharedPreferences p = getSharedPreferences(context, appWidgetId);

        SharedPreferences.Editor editor = p.edit();

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

        if (prefs.getIconsTheme() == null)
        {
            editor.remove(ICONS_THEME);
        } else
        {
            editor.putString(ICONS_THEME, prefs.getIconsTheme());
        }

        editor.putString(ICONS_ROTATE, prefs.getIconsRotate().name());
        editor.putBoolean(TITLES_HIDE, prefs.isTitlesHide());

        editor.putInt(WIDGET_BUTTON_1, prefs.getWidgetButton1());
        editor.putInt(WIDGET_BUTTON_2, prefs.getWidgetButton2());

        editor.commit();

    }

    private static Integer getColor(String key, SharedPreferences prefs) {
        if (!prefs.contains(key)) {
            return null;
        }
        return prefs.getInt(key, Color.WHITE);
    }

    public static String getLaunchComponentKey(int id) {
        return String.format(Locale.US, LAUNCH_COMPONENT, id);
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
        edit.commit();
    }

    public static boolean isFirstTime(Context context, int appWidgetId) {
        SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        return prefs.getBoolean(FIRST_TIME, true);
    }

    public static void setFirstTime(boolean value, Context context, int appWidgetId) {
        SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        Editor editor = prefs.edit();
        editor.putBoolean(FIRST_TIME, value);
        editor.commit();
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
        editor.commit();
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
            // TODO: Remove file
        }
    }

    public static void dropShortcutPreference(int cellId, int appWidgetId, Context context) {
        String key = getLaunchComponentKey(cellId);
        final SharedPreferences prefs = getSharedPreferences(context, appWidgetId);
        Editor edit = prefs.edit();
        edit.remove(key);
        edit.commit();
    }
}
