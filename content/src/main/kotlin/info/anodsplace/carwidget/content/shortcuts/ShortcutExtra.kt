package info.anodsplace.carwidget.content.shortcuts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.KeyEvent
import info.anodsplace.carwidget.content.InternalShortcutResources
import info.anodsplace.carwidget.content.R

class ShortcutTargetActivity(
    val settings: Class<*>,  // LookAndFeelActivity::class.java
    val switchInCar: Class<*>, //SwitchInCarActivity::class.java
    val runShortcut: Class<*>,  //ShortcutActivity::class.java
    val overlay: Class<*> // OverlayActivity::class.java
)

interface ShortcutResources {
    val activity: ShortcutTargetActivity
    val internalShortcuts: InternalShortcutResources
    val folderShortcutIcon: Int
}

sealed class InternalShortcut(val index: Int) {
    object SwitchInCar : InternalShortcut(index = 0)
    object DirectCall : InternalShortcut(index = 1)
    object PlayPause : InternalShortcut(index = 2)
    object Next : InternalShortcut(index = 3)
    object Previous : InternalShortcut(index = 4)

    companion object {
        val all = listOf(
            SwitchInCar,
            DirectCall,
            PlayPause,
            Next,
            Previous
        )

        fun titles(context: Context): Array<String> =
            context.resources.getStringArray(R.array.carwidget_shortcuts)
    }
}

object ShortcutExtra {
    const val EXTRA_MEDIA_BUTTON = "media_button"
    const val ACTION_MEDIA_BUTTON = "action_media_button"
    const val ACTION_FOLDER = "info.anodsplace.carwidget.action.FOLDER"
    const val EXTRA_PREFER_ICON_RESOURCE = "info.anodsplace.carwidget.extra.PREFER_ICON_RESOURCE"
}

fun InternalShortcut.fillIntent(
    intent: Intent,
    context: Context,
    resources: ShortcutResources
): Intent {
    when (this) {
        InternalShortcut.SwitchInCar -> intent.component =
            ComponentName(context, resources.activity.switchInCar)

        InternalShortcut.DirectCall -> {
            intent.action = Intent.ACTION_PICK
            intent.data = ContactsContract.Contacts.CONTENT_URI
        }

        InternalShortcut.PlayPause -> fillMediaButtonIntent(
            intent,
            context,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            resources.activity.runShortcut
        )

        InternalShortcut.Next -> fillMediaButtonIntent(
            intent,
            context,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            resources.activity.runShortcut
        )

        InternalShortcut.Previous -> fillMediaButtonIntent(
            intent,
            context,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            resources.activity.runShortcut
        )
    }
    return intent
}

private fun fillMediaButtonIntent(
    intent: Intent,
    context: Context,
    keyCode: Int,
    runShortcut: Class<*>
) {
    intent.component = ComponentName(context, runShortcut)
    intent.action = ShortcutExtra.ACTION_MEDIA_BUTTON
    intent.putExtra(ShortcutExtra.EXTRA_MEDIA_BUTTON, keyCode)
}
