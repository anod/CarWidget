package com.anod.car.home.prefs.model;

import com.anod.car.home.R;

/**
 * @author algavris
 * @date 09/04/2016.
 */
public class AppTheme {
    public static final int THEME_GRAY = 0;
    public static final int THEME_HOLO = 1;

    public static int getNameResource(int theme) {
        return (theme == THEME_GRAY) ? R.string.theme_gray : R.string.theme_holo;
    }

    public static int getMainResource(int theme) {
        return (theme == THEME_GRAY) ? R.style.AppThemeGray : R.style.AppThemeHolo;
    }

    public static int getTransparentResource(int theme) {
        return (theme == THEME_GRAY) ? R.style.AppThemeGray_Transparent
                : R.style.AppThemeHolo_Transparent;
    }

    public static int getNoActionBarResource(int theme) {
        return (theme == THEME_GRAY) ? R.style.AppThemeGray_NoActionBar
                : R.style.AppThemeHolo_NoActionBar;
    }

    public static int getMain() {
        return R.style.AppThemeGray;
    }

    public static int getBackgroundResource(int theme) {
        return (theme == THEME_GRAY) ? R.drawable.panel_item_bg_grey
                : R.drawable.panel_item_bg_dark;
    }
}
