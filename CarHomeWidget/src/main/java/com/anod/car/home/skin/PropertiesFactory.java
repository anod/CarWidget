package com.anod.car.home.skin;

import com.anod.car.home.prefs.preferences.Main;

public class PropertiesFactory {

	public static SkinProperties create(String skinName, boolean isKeyguard) {
		if (skinName.equals(Main.SKIN_WINDOWS7)) {
			return new MetroProperties(isKeyguard);
		} else if (skinName.equals(Main.SKIN_HOLO)) {
			return new HoloProperties(isKeyguard);
		} else if (skinName.equals(Main.SKIN_GLOSSY)) {
			return new GlossyProperties(isKeyguard);
		} else if (skinName.equals(Main.SKIN_BBB)) {
			return new BBBProperties(isKeyguard);
		}
		return new CarHomeProperties(isKeyguard);
	}
}
