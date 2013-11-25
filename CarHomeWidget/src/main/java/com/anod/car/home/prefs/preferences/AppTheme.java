package com.anod.car.home.prefs.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 10/7/13
 */
public class AppTheme {
	public static final int THEME_GRAY = 0;
	public static final int THEME_HOLO = 1;

	private static final String APP_THEME = "app_theme";


	public static int getNameResource(int theme) {
		return (theme == THEME_GRAY) ? R.string.theme_gray : R.string.theme_holo;
	}

	public static int getMainResource(int theme) {
		return (theme == THEME_GRAY) ? R.style.AppThemeGray : R.style.AppThemeHolo;
	}

	public static int getTransparentResource(int theme) {
		return (theme == THEME_GRAY) ? R.style.AppThemeGray_Transparent : R.style.AppThemeHolo_Transparent;
	}

	public static int getMain() {
		return R.style.AppThemeGray;
	}

	public static int getTheme(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(APP_THEME, THEME_GRAY);
	}

	public static void saveAppTheme(Context context, int theme) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt(APP_THEME, theme);
		edit.commit();
	}

	public static int getBackgroundResource(int theme) {
		return (theme == THEME_GRAY) ? R.drawable.panel_item_bg_grey : R.drawable.panel_item_bg_dark;
	}
}
