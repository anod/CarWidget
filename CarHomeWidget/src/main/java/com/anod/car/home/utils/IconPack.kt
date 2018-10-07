package com.anod.car.home.utils

import android.content.Intent

object IconPack {
    internal const val ACTION_ADW_PICK_ICON = "org.adw.launcher.icons.ACTION_PICK_ICON"
    internal const val THEME_CATEGORY = "com.anddoes.launcher.THEME"
}

fun Intent.forIconPack(): Intent {
    action = IconPack.ACTION_ADW_PICK_ICON
    return this
}

fun Intent.forIconTheme(): Intent {
    addCategory(IconPack.THEME_CATEGORY)
    action = Intent.ACTION_MAIN
    return this
}