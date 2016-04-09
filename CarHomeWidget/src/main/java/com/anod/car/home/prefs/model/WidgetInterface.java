package com.anod.car.home.prefs.model;

import com.anod.car.home.utils.BitmapTransform;

/**
 * @author algavris
 * @date 09/04/2016.
 */
public interface WidgetInterface {
    String SKIN_GLOSSY = "glossy";
    String SKIN_CARHOME = "carhome";
    String SKIN_WINDOWS7 = "windows7";
    String SKIN_HOLO = "holo";
    String SKIN_BBB = "blackbearblanc";
    String SKIN_CARDS = "cards";
    int FONT_SIZE_UNDEFINED = -1;
    int WIDGET_BUTTON_INCAR = 1;
    int WIDGET_BUTTON_SETTINGS = 2;
    int WIDGET_BUTTON_HIDDEN = 3;

    String getIconsTheme();

    boolean isSettingsTransparent();

    boolean isIncarTransparent();

    void setSettingsTransparent(boolean settingsTransparent);

    void setIncarTransparent(boolean incarTransparent);

    String getSkin();

    Integer getTileColor();

    boolean isIconsMono();

    Integer getIconsColor();

    String getIconsScale();

    int getFontColor();

    int getFontSize();

    int getBackgroundColor();

    void setSkin(String skin);

    void setTileColor(Integer tileColor);

    void setIconsMono(boolean iconsMono);

    void setIconsColor(Integer iconsColor);

    void setIconsScaleString(String iconsScale);

    void setFontColor(int fontColor);

    void setFontSize(int fontSize);

    void setBackgroundColor(int backgroundColor);

    void setIconsTheme(String iconsTheme);

    BitmapTransform.RotateDirection getIconsRotate();

    void setIconsRotate(BitmapTransform.RotateDirection iconsRotate);

    boolean isTitlesHide();

    void setTitlesHide(boolean titlesHide);

    int getWidgetButton1();

    void setWidgetButton1(int widgetButton1);

    int getWidgetButton2();

    void setWidgetButton2(int widgetButton2);
}
