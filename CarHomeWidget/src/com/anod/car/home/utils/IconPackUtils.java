package com.anod.car.home.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;

public class IconPackUtils {
	private static final String ACTION_ADW_PICK_ICON="org.adw.launcher.icons.ACTION_PICK_ICON";

	public static void fillAdwIconPackIntent(Intent intent) {
		intent.setAction(ACTION_ADW_PICK_ICON);
	}

}
