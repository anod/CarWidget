package com.anod.car.home.utils

import com.anod.car.home.ShortcutActivity
import com.anod.car.home.incar.SwitchInCarActivity
import com.anod.car.home.prefs.LookAndFeelActivity
import info.anodsplace.carwidget.content.InternalShortcutResources
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.ShortcutTargetActivity
import info.anodsplace.carwidget.skin.BaseProperties

class WidgetShortcutResource(
    override val activity: ShortcutTargetActivity,
    override val internalShortcuts: InternalShortcutResources
) : ShortcutResources {
    constructor() : this(
        activity = ShortcutTargetActivity(
            settings = LookAndFeelActivity::class.java,
            switchInCar = SwitchInCarActivity::class.java,
            runShortcut = ShortcutActivity::class.java
        ),
        internalShortcuts = BaseProperties.internalShortcutResourcesPrimary
    )
}