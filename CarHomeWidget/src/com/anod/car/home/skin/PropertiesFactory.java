package com.anod.car.home.skin;

import com.anod.car.home.prefs.PreferencesStorage;

public class PropertiesFactory {

	public static SkinProperties create(String skinName) {
		if (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) {
			return new MetroProperties();
		} else if (skinName.equals(PreferencesStorage.SKIN_HOLO)) {
			return new HoloProperties();
		} else if (skinName.equals(PreferencesStorage.SKIN_GLOSSY)) {
			return new GlossyProperties();
		} else if (skinName.equals(PreferencesStorage.SKIN_BBB)) {
			return new BBBProperties();
		}
		return new CarHomeProperties();
	}
}
