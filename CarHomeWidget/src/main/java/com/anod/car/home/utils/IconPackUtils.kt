package com.anod.car.home.utils

import android.content.Intent

object IconPackUtils {

    private val ACTION_ADW_PICK_ICON = "org.adw.launcher.icons.ACTION_PICK_ICON"

    private val ACTION_ADW_THEMES = "org.adw.launcher.THEMES"

    private val THEME_CATEGORY = "com.anddoes.launcher.THEME"

    fun fillAdwIconPackIntent(intent: Intent) {
        intent.action = ACTION_ADW_PICK_ICON
        //	intent.addCategory(THEME_CATEGORY);
    }

    fun fillAdwThemeIntent(intent: Intent) {
        //intent.setAction(ACTION_ADW_THEMES);
        intent.addCategory(THEME_CATEGORY)
        intent.action = Intent.ACTION_MAIN
    }
}
