package com.anod.car.home.prefs.model


import com.anod.car.home.prefs.preferences.Main

/**
 * @author algavris
 * @date 19/03/2016.
 */
object PrefsMigrate {

    fun migrateMain(widget: WidgetSettings, prefs: Main) {
        widget.skin = prefs.skin
        widget.isTitlesHide = prefs.isTitlesHide

        widget.fontColor = prefs.fontColor
        widget.fontSize = prefs.fontSize

        widget.backgroundColor = prefs.backgroundColor
        widget.tileColor = prefs.tileColor

        widget.iconsColor = prefs.iconsColor
        widget.isIconsMono = prefs.isIconsMono
        widget.iconsRotate = prefs.iconsRotate
        widget.setIconsScaleString(prefs.iconsScale)
        widget.iconsTheme = prefs.iconsTheme

        widget.isIncarTransparent = prefs.isIncarTransparent
        widget.isSettingsTransparent = prefs.isSettingsTransparent

        widget.widgetButton1 = prefs.widgetButton1
        widget.widgetButton2 = prefs.widgetButton2
    }
}
