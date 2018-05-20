package com.anod.car.home.prefs.preferences;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;

import com.anod.car.home.R;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.utils.BitmapTransform.RotateDirection;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class WidgetMigrateStorage {

    public static final String CMP_NUMBER = "cmp-number-%d";
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
    private static final String ICONS_THEME = "icons-theme-%d";
    public static final String ICONS_ROTATE = "icons-rotate-%d";
    public static final String TITLES_HIDE = "titles-hide-%d";
    public static final String WIDGET_BUTTON_1 = "widget-button-1-%d";
    public static final String WIDGET_BUTTON_2 = "widget-button-2-%d";
    private static final String ICONS_DEF_VALUE = "5";

    public static Main loadMain(Context context, int appWidgetId) {
        final WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        Resources res = context.getResources();

        Main p = new Main();
        String skinName = prefs.getString(SKIN, Main.SKIN_CARDS);
        p.setSkin(skinName);

        int defTileColor = ResourcesCompat.getColor(res, R.color.w7_tale_default_background, null);
        int tileColor = prefs.getInt(BUTTON_COLOR, defTileColor);
        p.setTileColor(tileColor);

        p.setIconsScaleString(prefs.getString(ICONS_SCALE, ICONS_DEF_VALUE));
        p.setIconsMono(prefs.getBoolean(ICONS_MONO, false));
        p.setBackgroundColor(prefs.getInt(BG_COLOR, ResourcesCompat.getColor(res, R.color.default_background, null)));
        p.setIconsColor(prefs.getColor(ICONS_COLOR));
        p.setFontColor(prefs.getInt(FONT_COLOR, ResourcesCompat.getColor(res, R.color.default_font_color, null)));
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
            String key = WidgetMigrateStorage.getLaunchComponentName(i, appWidgetId);
            long id = prefs.getLong(key, Shortcut.idUnknown);
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

    public static boolean isFirstTime(Context context, int appWidgetId) {
        WidgetSharedPreferences prefs = new WidgetSharedPreferences(context);
        prefs.setAppWidgetId(appWidgetId);
        return prefs.getBoolean(FIRST_TIME, true);
    }

    static class WidgetSharedPreferences /* implements SharedPreferences */ {

        private final SharedPreferences mPrefs;

        private int mAppWidgetId;

        private WidgetEditor mWidgetEdit;

        public WidgetSharedPreferences(Context context) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        public void setAppWidgetId(int appWidgetId) {
            mAppWidgetId = appWidgetId;
        }

        public static String getName(String prefName, int aAppWidgetId) {
            return String.format(prefName, aAppWidgetId);
        }

        public static String getListName(String prefName, int listId, int aAppWidgetId) {
            return String.format(prefName, aAppWidgetId, listId);
        }

        @SuppressLint("CommitPrefEdits")
        public WidgetEditor edit() {
            if (mWidgetEdit == null) {
                Editor edit = mPrefs.edit();
                mWidgetEdit = new WidgetEditor(mAppWidgetId, edit);
            }
            return mWidgetEdit;
        }

        public boolean getBoolean(String key, boolean defValue) {
            String keyId = getName(key, mAppWidgetId);
            return mPrefs.getBoolean(keyId, defValue);
        }

        public boolean getBoolean(String key, int listId, boolean defValue) {
            String keyId = getListName(key, listId, mAppWidgetId);
            return mPrefs.getBoolean(keyId, defValue);
        }

        public float getFloat(String key, float defValue) {
            String keyId = getName(key, mAppWidgetId);
            return mPrefs.getFloat(keyId, defValue);
        }

        public int getInt(String key, int defValue) {
            String keyId = getName(key, mAppWidgetId);
            return mPrefs.getInt(keyId, defValue);
        }

        public long getLong(String key, long defValue) {
            String keyId = getName(key, mAppWidgetId);
            return mPrefs.getLong(keyId, defValue);
        }

        public String getString(String key, String defValue) {
            String keyId = getName(key, mAppWidgetId);
            return mPrefs.getString(keyId, defValue);
        }

        public Integer getColor(String key) {
            String prefName = getName(key, mAppWidgetId);
            if (!mPrefs.contains(prefName)) {
                return null;
            }
            return mPrefs.getInt(prefName, Color.WHITE);
        }

        public ComponentName getComponentName(String key) {
            String compString = getString(key, null);
            if (compString == null) {
                return null;
            }
            String[] compParts = compString.split("/");
            return new ComponentName(compParts[0], compParts[1]);
        }

        public final class WidgetEditor implements Editor {

            private final Editor mEdit;

            private int mEditAppWidgetId;

            public WidgetEditor(int appWidgetId, Editor edit) {
                mEdit = edit;
                mEditAppWidgetId = appWidgetId;
            }

            public void setAppWidgetId(int appWidgetId) {
                mEditAppWidgetId = appWidgetId;
            }

            @Override
            public void apply() {
                mEdit.commit();
            }

            @Override
            public Editor clear() {
                mEdit.clear();
                return this;
            }

            @Override
            public boolean commit() {
                return mEdit.commit();
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                String keyId = getName(key, mEditAppWidgetId);
                mEdit.putBoolean(keyId, value);
                return this;
            }

            public Editor putBoolean(String key, int listId, boolean value) {
                String keyId = getListName(key, listId, mEditAppWidgetId);
                mEdit.putBoolean(keyId, value);
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                String keyId = getName(key, mEditAppWidgetId);
                mEdit.putFloat(keyId, value);
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                String keyId = getName(key, mEditAppWidgetId);
                mEdit.putInt(keyId, value);
                return this;
            }

            /**
             * @param key
             * @param value
             * @return
             */
            public WidgetEditor putIntOrRemove(String key, int value) {
                String keyId = getName(key, mEditAppWidgetId);
                if (value > 0) {
                    mEdit.putInt(keyId, value);
                } else {
                    mEdit.remove(keyId);
                }
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                String keyId = getName(key, mEditAppWidgetId);
                mEdit.putLong(keyId, value);
                return this;
            }

            @Override
            public Editor putString(String key, String value) {
                String keyId = getName(key, mEditAppWidgetId);
                mEdit.putString(keyId, value);
                return this;
            }

            public WidgetEditor putStringOrRemove(String key, String value) {
                String keyId = getName(key, mEditAppWidgetId);
                if (value != null) {
                    mEdit.putString(keyId, value);
                } else {
                    mEdit.remove(keyId);
                }
                return this;
            }

            public WidgetEditor putComponentOrRemove(String key, ComponentName component) {
                String keyId = getName(key, mEditAppWidgetId);
                if (component != null) {
                    String value = component.getPackageName() + "/" + component.getClassName();
                    mEdit.putString(keyId, value);
                } else {
                    mEdit.remove(keyId);
                }
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> value) {
                //String keyId = getName(key, mEditAppWidgetId);
                //mEdit.putStringSet(keyId, value);
                throw new IllegalAccessError("Not implemented");
                //return this;
            }

            @Override
            public Editor remove(String key) {
                String keyId = getName(key, mEditAppWidgetId);
                mEdit.remove(keyId);
                return this;
            }

            public Editor remove(String key, int listId) {
                String keyId = getListName(key, listId, mEditAppWidgetId);
                mEdit.remove(keyId);
                return this;
            }
        }

    }
}
