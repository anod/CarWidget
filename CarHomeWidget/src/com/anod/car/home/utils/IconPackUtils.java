package com.anod.car.home.utils;

import android.app.Activity;
import android.content.Intent;

public class IconPackUtils {
	private static final String ACTION_ADW_PICK_ICON="org.adw.launcher.icons.ACTION_PICK_ICON";

	public static void startAdwIconPackChooser(int requestCode, Activity activity) {
		Intent chooseIntent = new Intent(ACTION_ADW_PICK_ICON);
		Utils.startActivityForResultSafetly(Intent.createChooser(chooseIntent, "Select icon pack"), requestCode, activity	);
	}

}
