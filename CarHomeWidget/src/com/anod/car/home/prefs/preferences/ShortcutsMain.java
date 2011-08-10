package com.anod.car.home.prefs.preferences;

import java.util.HashMap;

import com.anod.car.home.model.ShortcutInfo;

public class ShortcutsMain {
	private HashMap<Integer,ShortcutInfo> shortcuts;
	private Main main;
	
	public ShortcutsMain(HashMap<Integer, ShortcutInfo> shortcuts, Main main) {
		this.shortcuts = shortcuts;
		this.main = main;
	}

	public HashMap<Integer, ShortcutInfo> getShortcuts() {
		return shortcuts;
	}

	public void setShortcuts(HashMap<Integer, ShortcutInfo> shortcuts) {
		this.shortcuts = shortcuts;
	}

	public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}
	
}
