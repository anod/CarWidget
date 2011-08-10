package com.anod.car.home.prefs.preferences;

import java.util.HashMap;

import com.anod.car.home.model.ShortcutInfo;

public class Main {
	private HashMap<Integer,ShortcutInfo> shortcuts;
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
	public HashMap<Integer,ShortcutInfo> getLauncherComponents() {
		return shortcuts;
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
	public void setShortcuts(HashMap<Integer,ShortcutInfo> shortcuts) {
		this.shortcuts = shortcuts;
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
}