package com.anod.car.home.prefs.preferences;

import com.anod.car.home.prefs.model.WidgetInterface;
import com.anod.car.home.utils.BitmapTransform.RotateDirection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;


public class Main implements Serializable, com.anod.car.home.prefs.model.WidgetInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String skin;

    private Integer tileColor;

    private boolean iconsMono;

    private Integer iconsColor;

    private String iconsScale;

    private int fontColor;

    private int fontSize;

    private int backgroundColor;

    private boolean settingsTransparent;

    private boolean incarTransparent;

    private String iconsTheme;

    private RotateDirection iconsRotate = RotateDirection.NONE;

    private boolean titlesHide;

    private int widgetButton1;

    private int widgetButton2;


    @Override
    public String getIconsTheme() {
        return iconsTheme;
    }

    @Override
    public boolean isSettingsTransparent() {
        return settingsTransparent;
    }

    @Override
    public boolean isIncarTransparent() {
        return incarTransparent;
    }

    @Override
    public void setSettingsTransparent(boolean settingsTransparent) {
        this.settingsTransparent = settingsTransparent;
    }

    @Override
    public void setIncarTransparent(boolean incarTransparent) {
        this.incarTransparent = incarTransparent;
    }

    @Override
    public String getSkin() {
        return skin;
    }

    @Override
    public Integer getTileColor() {
        return tileColor;
    }

    @Override
    public boolean isIconsMono() {
        return iconsMono;
    }

    @Override
    public Integer getIconsColor() {
        return iconsColor;
    }

    @Override
    public String getIconsScale() {
        return iconsScale;
    }

    @Override
    public int getFontColor() {
        return fontColor;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setSkin(String skin) {
        this.skin = skin;
    }

    @Override
    public void setTileColor(Integer tileColor) {
        this.tileColor = tileColor;
    }

    @Override
    public void setIconsMono(boolean iconsMono) {
        this.iconsMono = iconsMono;
    }

    @Override
    public void setIconsColor(Integer iconsColor) {
        this.iconsColor = iconsColor;
    }

    @Override
    public void setIconsScaleString(String iconsScale) {
        this.iconsScale = iconsScale;
    }

    @Override
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    @Override
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void setIconsTheme(String iconsTheme) {
        this.iconsTheme = iconsTheme;
    }

    @Override
    public final RotateDirection getIconsRotate() {
        return iconsRotate;
    }

    @Override
    public final void setIconsRotate(RotateDirection iconsRotate) {
        this.iconsRotate = iconsRotate;
    }

    @Override
    public final boolean isTitlesHide() {
        return titlesHide;
    }

    @Override
    public final void setTitlesHide(boolean titlesHide) {
        this.titlesHide = titlesHide;
    }

    @Override
    public int getWidgetButton1() {
        return widgetButton1;
    }

    @Override
    public void setWidgetButton1(int widgetButton1) {
        this.widgetButton1 = widgetButton1;
    }

    @Override
    public int getWidgetButton2() {
        return widgetButton2;
    }

    @Override
    public void setWidgetButton2(int widgetButton2) {
        this.widgetButton2 = widgetButton2;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (iconsRotate == null) {
            iconsRotate = RotateDirection.NONE;
        }
        if (widgetButton1 == 0) {
            widgetButton1 = WidgetInterface.Companion.WIDGET_BUTTON_INCAR;
        }
        if (widgetButton2 == 0) {
            widgetButton2 = WidgetInterface.Companion.WIDGET_BUTTON_SETTINGS;
        }
    }
}