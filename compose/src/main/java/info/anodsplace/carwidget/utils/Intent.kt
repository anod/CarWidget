package info.anodsplace.carwidget.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.carwidget.content.shortcuts.InternalShortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.fillIntent
import info.anodsplace.graphics.DrawableUri

fun Intent.forSettings(context: Context, appWidgetId: Int, target: ShortcutResources): Intent {
    component = ComponentName(context, target.activity.settings)
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), appWidgetId.toString())
    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

fun Intent.forProVersion(): Intent {
    action = Intent.ACTION_VIEW
    data = Uri.parse(String.format("market://details?id=%s", Version.PRO_PACKAGE_NAME))
    return this
}

fun Intent.forPickShortcutLocal(shortcut: InternalShortcut, title: String, icnResId: Int, ctx: Context, target: ShortcutResources): Intent {
    val shortcutIntent = shortcut.fillIntent(Intent(), ctx, target)
    shortcutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY

    val intent = commonPickShortcutIntent(this, title, shortcutIntent)
    val iconResource = Intent.ShortcutIconResource.fromContext(ctx, icnResId)
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
    return intent
}

private fun commonPickShortcutIntent(thisIntent: Intent, title: String, shortcutIntent: Intent): Intent {
    thisIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
    thisIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
    return thisIntent
}

fun Intent.forAppSettings(context: Context): Intent {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.parse("package:${context.packageName}")
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

fun Intent.resolveDirectCall(contactUri: Uri, context: Context): Intent? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    )

    val cursor: Cursor?
    try {
        cursor = context.contentResolver.query(contactUri, projection, null, null, null)
    } catch (e: IllegalArgumentException) {
        AppLog.e(e)
        return null
    }

    val maxIconSize = UtilitiesBitmap.getIconMaxSize(context)
    val targetDensity = UtilitiesBitmap.getTargetDensity(context)

    // If the cursor returned is valid, get the phone number
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            val number = cursor.getString(numberIndex)
            val name = cursor.getString(nameIndex)
            val photoUri = cursor.getString(photoIndex)

            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")

            val intent = commonPickShortcutIntent(this, name, callIntent)
            if (photoUri.isNotEmpty()) {
                val d = DrawableUri(context).resolve(Uri.parse(photoUri), maxIconSize, targetDensity)
                if (d != null) {
                    val bitmap = UtilitiesBitmap.createHiResIconBitmap(d, context)
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
                }
            }
            return intent
        }
        cursor.close()
    }
    return null
}