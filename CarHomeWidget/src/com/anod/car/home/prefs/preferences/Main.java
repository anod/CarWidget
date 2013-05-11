package com.anod.car.home.prefs.preferences;

import java.io.Serializable;

import com.anod.car.home.utils.BitmapTransform.RotateDirection;


public class Main implements Serializable {
	public static final String SKIN_GLOSSY = "glossy";
	public static final String SKIN_CARHOME = "carhome";
	public static final String SKIN_WINDOWS7 = "windows7";
	public static final String SKIN_HOLO = "holo";
	public static final String SKIN_BBB = "blackbearblanc";
	
	public static final int FONT_SIZE_UNDEFINED = -1;
	
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
	private RotateDirection iconsRotate;
	private boolean titlesHide;
	
	public String getIconsTheme() {
		return iconsTheme;
	}
	public boolean isSettingsTransparent() {
		return settingsTransparent;
	}
	public boolean isIncarTransparent() {
		return incarTransparent;
	}
	public void setSettingsTransparent(boolean settingsTransparent) {
		this.settingsTransparent = settingsTransparent;
	}
	public void setIncarTransparent(boolean incarTransparent) {
		this.incarTransparent = incarTransparent;
	}
	public String getSkin() {
		return skin;
	}
	public Integer getTileColor() {
		return tileColor;
	}
	public boolean isIconsMono() {
		return iconsMono;
	}
	public Integer getIconsColor() {
		return iconsColor;
	}
	public String getIconsScale() {
		return iconsScale;
	}
	public int getFontColor() {
		return fontColor;
	}
	public int getFontSize() {
		return fontSize;
	}
	public int getBackgroundColor() {
		return backgroundColor;
	}
	public void setSkin(String skin) {
		this.skin = skin;
	}
	public void setTileColor(Integer tileColor) {
		this.tileColor = tileColor;
	}
	public void setIconsMono(boolean iconsMono) {
		this.iconsMono = iconsMono;
	}
	public void setIconsColor(Integer iconsColor) {
		this.iconsColor = iconsColor;
	}
	public void setIconsScaleString(String iconsScale) {
		this.iconsScale = iconsScale;
	}
	public void setFontColor(int fontColor) {
		this.fontColor = fontColor;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public void setIconsTheme(String iconsTheme) {
		this.iconsTheme = iconsTheme;
	}
	public final RotateDirection getIconsRotate() {
		return iconsRotate;
	}
	
	public final void setIconsRotate(RotateDirection iconsRotate) {
		this.iconsRotate = iconsRotate;
	}
	public final boolean isTitlesHide() {
		return titlesHide;
	}
	public final void setTitlesHide(boolean titlesHide) {
		this.titlesHide = titlesHide;
	}
}