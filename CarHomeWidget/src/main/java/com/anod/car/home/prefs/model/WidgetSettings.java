package com.anod.car.home.prefs.model;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import com.anod.car.home.R;
import com.anod.car.home.utils.BitmapTransform;

import java.io.IOException;

/**
 * @author algavris
 * @date 09/04/2016.
 */
public class WidgetSettings extends ChangeableSharedPreferences implements WidgetInterface {
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
    private static final String ICONS_THEME = "icons-theme";
    public static final String ICONS_ROTATE = "icons-rotate";
    public static final String TITLES_HIDE = "titles-hide";
    private static final String WIDGET_BUTTON_1 = "widget-button-1";
    private static final String WIDGET_BUTTON_2 = "widget-button-2";

    private static final String ICONS_DEF_VALUE = "5";

    private final Resources mResources;

    public WidgetSettings(SharedPreferences prefs, Resources resources) {
        super(prefs);
        mResources = resources;
    }

    public boolean isFirstTime() {
        return mPrefs.getBoolean(FIRST_TIME, true);
    }

    public void setFirstTime(boolean value) {
        putChange(FIRST_TIME, value);
    }

    @Override
    public String getIconsTheme() {
        return mPrefs.getString(ICONS_THEME, null);
    }

    @Override
    public boolean isSettingsTransparent() {
        return mPrefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false);
    }

    @Override
    public boolean isIncarTransparent() {
        return mPrefs.getBoolean(TRANSPARENT_BTN_INCAR, false);
    }

    @Override
    public void setSettingsTransparent(boolean settingsTransparent) {
        putChange(TRANSPARENT_BTN_SETTINGS, settingsTransparent);
    }

    @Override
    public void setIncarTransparent(boolean incarTransparent) {
        putChange(TRANSPARENT_BTN_INCAR, incarTransparent);
    }

    @Override
    public String getSkin() {
        return mPrefs.getString(SKIN, WidgetInterface.SKIN_CARDS);
    }

    @Override
    public Integer getTileColor() {
        int defTileColor = ResourcesCompat.getColor(mResources, R.color.w7_tale_default_background, null);
        return mPrefs.getInt(BUTTON_COLOR, defTileColor);
    }

    @Override
    public boolean isIconsMono() {
        return mPrefs.getBoolean(ICONS_MONO, false);
    }

    @Override
    public Integer getIconsColor() {
        return getColor(ICONS_COLOR, mPrefs);
    }

    @Override
    public String getIconsScale() {
        return mPrefs.getString(ICONS_SCALE, ICONS_DEF_VALUE);
    }

    @Override
    public int getFontColor() {
        return mPrefs.getInt(FONT_COLOR, ResourcesCompat.getColor(mResources, R.color.default_font_color, null));
    }

    @Override
    public int getFontSize() {
        return mPrefs.getInt(FONT_SIZE, WidgetInterface.FONT_SIZE_UNDEFINED);
    }

    @Override
    public int getBackgroundColor() {
        return mPrefs.getInt(BG_COLOR, ResourcesCompat.getColor(mResources, R.color.default_background, null));
    }

    @Override
    public void setSkin(String skin) {
        putChange(SKIN, skin);
    }

    @Override
    public void setTileColor(Integer tileColor) {
        putChange(BUTTON_COLOR, tileColor);
    }

    @Override
    public void setIconsMono(boolean iconsMono) {
        putChange(ICONS_MONO, iconsMono);
    }

    @Override
    public void setIconsColor(Integer iconsColor) {
        putChange(ICONS_COLOR, iconsColor);
    }

    @Override
    public void setIconsScaleString(String iconsScale) {
        putChange(ICONS_SCALE, iconsScale);
    }

    @Override
    public void setFontColor(int fontColor) {
        putChange(FONT_COLOR, fontColor);
    }

    @Override
    public void setFontSize(int fontSize) {
        putChange(FONT_SIZE, fontSize);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        putChange(BG_COLOR, backgroundColor);
    }

    @Override
    public void setIconsTheme(String iconsTheme) {
        putChange(ICONS_THEME, iconsTheme);
    }

    @Override
    public BitmapTransform.RotateDirection getIconsRotate() {
        return BitmapTransform.RotateDirection
                .valueOf(mPrefs.getString(ICONS_ROTATE, BitmapTransform.RotateDirection.NONE.name()));
    }

    @Override
    public void setIconsRotate(BitmapTransform.RotateDirection iconsRotate) {
        putChange(ICONS_ROTATE, iconsRotate.name());
    }

    @Override
    public boolean isTitlesHide() {
        return mPrefs.getBoolean(TITLES_HIDE, false);
    }

    @Override
    public void setTitlesHide(boolean titlesHide) {
        putChange(TITLES_HIDE, titlesHide);
    }

    @Override
    public int getWidgetButton1() {
        return mPrefs.getInt(WIDGET_BUTTON_1, WidgetInterface.WIDGET_BUTTON_INCAR);
    }

    @Override
    public void setWidgetButton1(int widgetButton1) {
        putChange(WIDGET_BUTTON_1, widgetButton1);
    }

    @Override
    public int getWidgetButton2() {
        return mPrefs.getInt(WIDGET_BUTTON_2, WidgetInterface.WIDGET_BUTTON_SETTINGS);
    }

    @Override
    public void setWidgetButton2(int widgetButton2) {
        putChange(WIDGET_BUTTON_2, widgetButton2);
    }

    private static Integer getColor(String key, SharedPreferences prefs) {
        if (!prefs.contains(key)) {
            return null;
        }
        return prefs.getInt(key, Color.WHITE);
    }

    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(FIRST_TIME).value(isFirstTime());

        writer.name(SKIN).value(getSkin());

        writer.name(BG_COLOR).value(getBackgroundColor());
        writer.name(BUTTON_COLOR).value(getTileColor());

        writer.name(ICONS_MONO).value(isIconsMono());
        writer.name(ICONS_COLOR).value(getIconsColor());
        writer.name(ICONS_SCALE).value(getIconsScale());
        writer.name(ICONS_THEME).value(getIconsTheme());
        writer.name(ICONS_ROTATE).value(getIconsRotate().name());

        writer.name(FONT_SIZE).value(getFontSize());
        writer.name(FONT_COLOR).value(getFontColor());

        writer.name(TRANSPARENT_BTN_SETTINGS).value(isSettingsTransparent());
        writer.name(TRANSPARENT_BTN_INCAR).value(isIncarTransparent());

        writer.name(TITLES_HIDE).value(isTitlesHide());

        writer.name(WIDGET_BUTTON_1).value(getWidgetButton1());
        writer.name(WIDGET_BUTTON_2).value(getWidgetButton2());

        writer.endObject();
    }

    public void readJson(JsonReader reader) throws IOException {
        reader.beginObject();

        SimpleArrayMap<String, JsonToken> types = new SimpleArrayMap<>();
        types.put(FIRST_TIME, JsonToken.BOOLEAN);
        types.put(SKIN, JsonToken.STRING);

        types.put(BG_COLOR, JsonToken.NUMBER);
        types.put(BUTTON_COLOR, JsonToken.NUMBER);

        types.put(ICONS_MONO, JsonToken.BOOLEAN);
        types.put(ICONS_COLOR, JsonToken.NUMBER);
        types.put(ICONS_SCALE, JsonToken.STRING);
        types.put(ICONS_THEME, JsonToken.STRING);
        types.put(ICONS_ROTATE, JsonToken.STRING);

        types.put(FONT_SIZE, JsonToken.NUMBER);
        types.put(FONT_COLOR, JsonToken.NUMBER);

        types.put(TRANSPARENT_BTN_SETTINGS, JsonToken.BOOLEAN);
        types.put(TRANSPARENT_BTN_INCAR, JsonToken.BOOLEAN);

        types.put(TITLES_HIDE, JsonToken.BOOLEAN);

        types.put(WIDGET_BUTTON_1, JsonToken.NUMBER);
        types.put(WIDGET_BUTTON_2, JsonToken.NUMBER);

        JsonReaderHelper.readValues(reader, types, this);

        reader.endObject();
    }
}
