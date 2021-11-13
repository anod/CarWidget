package info.anodsplace.carwidget.content.shortcuts

class InternalShortcutResources(
    val icons: IntArray
)

class ShortcutTargetActivity(
    val settings: Class<*>,  // LookAndFeelActivity::class.java)
    val switchInCar: Class<*>, //SwitchInCarActivity::class.java)
    val runShortcut: Class<*>  //ShortcutActivity::class.java)
)

interface ShortcutResources {
    val activity: ShortcutTargetActivity
    val internalShortcuts: InternalShortcutResources
}

object ShortcutExtra {
    const val EXTRA_MEDIA_BUTTON = "media_button"
    const val ACTION_MEDIA_BUTTON = "action_media_button"
}