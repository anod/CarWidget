package com.anod.car.home.utils

import com.anod.car.home.R
import com.anod.car.home.ShortcutActivity
import com.anod.car.home.incar.SwitchInCarActivity
import com.anod.car.home.prefs.LookAndFeelActivity
import info.anodsplace.carwidget.content.shortcuts.InternalShortcutResources
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.ShortcutTargetActivity

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
        internalShortcuts = InternalShortcutResources(
            icons = intArrayOf(
                R.drawable.ic_launcher_carwidget,
                R.drawable.ic_shortcut_call,
                R.drawable.ic_shortcut_play,
                R.drawable.ic_shortcut_next,
                R.drawable.ic_shortcut_previous
            )
        )
    )
}