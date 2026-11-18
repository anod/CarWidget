package info.anodsplace.carwidget.content.shortcuts

import android.content.Intent

/** Allowed field values shared by the shortcut editor and the launch sanitizer. */
object ShortcutIntentPolicy {
    const val ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED"

    val actions: Set<String> = setOf(
        Intent.ACTION_MAIN,
        Intent.ACTION_VIEW,
        Intent.ACTION_DIAL,
        Intent.ACTION_CALL,
        ACTION_CALL_PRIVILEGED,
        Intent.ACTION_SENDTO,
        Intent.ACTION_SEND,
        Intent.ACTION_SEND_MULTIPLE,
        Intent.ACTION_PICK,
        Intent.ACTION_EDIT,
        Intent.ACTION_INSERT,
        Intent.ACTION_DELETE,
        Intent.ACTION_SEARCH,
        Intent.ACTION_WEB_SEARCH,
        Intent.ACTION_ASSIST,
        Intent.ACTION_VOICE_COMMAND,
        Intent.ACTION_APPLICATION_PREFERENCES,
        Intent.ACTION_SHOW_APP_INFO,
        Intent.ACTION_GET_CONTENT,
        Intent.ACTION_QUICK_VIEW,
        "android.provider.action.QUICK_CONTACT",
        "com.google.android.googlequicksearchbox.MUSIC_SEARCH",
        "android.media.action.MEDIA_PLAY_FROM_SEARCH",
        Intent.ACTION_MEDIA_BUTTON,
        ShortcutExtra.ACTION_MEDIA_BUTTON,
        ShortcutExtra.ACTION_FOLDER,
    )

    val categories: Set<String> = setOf(
        Intent.CATEGORY_DEFAULT,
        Intent.CATEGORY_LAUNCHER,
        Intent.CATEGORY_BROWSABLE,
        Intent.CATEGORY_APP_MAPS,
        Intent.CATEGORY_APP_EMAIL,
        Intent.CATEGORY_APP_BROWSER,
        Intent.CATEGORY_APP_CALENDAR,
        Intent.CATEGORY_APP_CONTACTS,
        Intent.CATEGORY_APP_MESSAGING,
        Intent.CATEGORY_APP_MUSIC,
    )

    val mimeTypes: Set<String> = setOf(
        "vnd.android.cursor.item/contact",
        "vnd.android.cursor.item/phone_v2",
        "vnd.android.cursor.item/person",
    )

    val flags: Set<Int> = setOf(
        Intent.FLAG_ACTIVITY_NEW_TASK,
        Intent.FLAG_ACTIVITY_NO_HISTORY,
        Intent.FLAG_ACTIVITY_CLEAR_TOP,
        Intent.FLAG_ACTIVITY_SINGLE_TOP,
        Intent.FLAG_FROM_BACKGROUND,
        Intent.FLAG_ACTIVITY_NEW_DOCUMENT, // Same bit as FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET.
        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
        Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT,
    )
}
