package com.anod.car.home.utils;

import android.content.Intent;

public class IconPackUtils {
	private static final String ACTION_ADW_PICK_ICON="org.adw.launcher.icons.ACTION_PICK_ICON";
	private static final String ACTION_ADW_THEMES="org.adw.launcher.THEMES";
	public static void fillAdwIconPackIntent(Intent intent) {
		intent.setAction(ACTION_ADW_PICK_ICON);
	}

	public static void fillAdwThemeIntent(Intent intent) {
		intent.setAction(ACTION_ADW_THEMES);
	}
}
