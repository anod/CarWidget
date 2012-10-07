package com.anod.car.home.utils;

import java.util.List;

import com.anod.car.home.prefs.ShortcutEditActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class IntentUtils {
	private static final String SCHEME = "package";
	private static final String APP_PKG_NAME_22 = "pkg";
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	/**
	 * 
	 * @param packageName
	 * @return
	 */
	public static Intent createApplicationDetailsIntent(String packageName) {
		Intent intent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) { // above
																		// 2.3
			intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(SCHEME, packageName, null));
		} else { // below 2.3
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
			intent.putExtra(APP_PKG_NAME_22, packageName);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		return intent;
	}

	
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	public static Intent createShortcutEditIntent(Context context,int cellId, long shortcutId) {
		Intent editIntent = new Intent(context, ShortcutEditActivity.class);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, shortcutId);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, cellId);
		return editIntent;
	}

}