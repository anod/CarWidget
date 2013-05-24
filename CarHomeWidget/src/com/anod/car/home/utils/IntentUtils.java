package com.anod.car.home.utils;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.anod.car.home.prefs.Configuration;
import com.anod.car.home.prefs.PickShortcutUtils;
import com.anod.car.home.prefs.ShortcutEditActivity;

public class IntentUtils {
	private static final String SCHEME = "package";
	private static final String APP_PKG_NAME_22 = "pkg";
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
	private static final String DETAIL_MARKET_URL = "market://details?id=%s";


	public static Intent createSettingsIntent(Context context, int appWidgetId, int cellId) {
		Intent intent = new Intent(context, Configuration.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		if (cellId != PickShortcutUtils.INVALID_CELL_ID) {
			intent.putExtra(PickShortcutUtils.EXTRA_CELL_ID, cellId);
		}
		String path = appWidgetId + " - " + cellId;
		Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),path);
		intent.setData(data);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		return intent;
	}
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
		return !list.isEmpty();
	}
	
	public static Intent createShortcutEditIntent(Context context,int cellId, long shortcutId) {
		Intent editIntent = new Intent(context, ShortcutEditActivity.class);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, shortcutId);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, cellId);
		return editIntent;
	}


	public static Intent createProVersionIntent() {
		String url = DETAIL_MARKET_URL;
		Uri uri = Uri.parse(String.format(url, Version.PRO_PACKAGE_NAME));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(uri);
		return intent;
	}
}