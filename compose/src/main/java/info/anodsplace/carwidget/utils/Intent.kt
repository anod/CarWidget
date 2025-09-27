package info.anodsplace.carwidget.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.net.toUri
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.carwidget.content.shortcuts.InternalShortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.fillIntent
import info.anodsplace.graphics.DrawableUri

fun Intent.forSettings(context: Context, appWidgetId: Int, target: ShortcutResources): Intent {
    component = ComponentName(context, target.activity.settings)
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    data = Uri.withAppendedPath("com.anod.car.home://widget/id/".toUri(), appWidgetId.toString())
    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

fun Intent.forPickShortcutLocal(shortcut: InternalShortcut, title: String, icnResId: Int, ctx: Context, target: ShortcutResources): Intent {
    val shortcutIntent = shortcut.fillIntent(Intent(), ctx, target)
    shortcutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY

    val intent = commonPickShortcutIntent(this, title, shortcutIntent)
    val iconResource = Intent.ShortcutIconResource.fromContext(ctx, icnResId)
    @Suppress("DEPRECATION")
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
    return intent
}

fun Intent.forFolder(title: String,ctx: Context, target: ShortcutResources): ShortcutIntent {
    val folderIntent = Intent().apply {
        action = ShortcutExtra.ACTION_FOLDER
        component = ComponentName(ctx, target.activity.overlay)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        @Suppress("DEPRECATION")
        putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
    }
    val intent = commonPickShortcutIntent(this, title, folderIntent)
    val iconResource = Intent.ShortcutIconResource.fromContext(ctx, target.folderShortcutIcon)
    @Suppress("DEPRECATION")
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
    return ShortcutIntent(intent, isApp = false)
}

fun Intent.forIconPackResult(icon: Bitmap?, iconResourceId: Int?, uri: Uri?, context: Context): Intent {
    if (icon != null) {
        putExtra("icon", icon)
    }
    // Also add the direct icon resource ID to the intent for launchers that support it
    if (iconResourceId != null) {
        val iconRes = ShortcutIconResource.fromContext(context, iconResourceId)
        putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes)
    }
    data = uri
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    setDataAndType(uri, "image/png")
    return this
}


private fun commonPickShortcutIntent(thisIntent: Intent, title: String, shortcutIntent: Intent): Intent {
    @Suppress("DEPRECATION")
    thisIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
    @Suppress("DEPRECATION")
    thisIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
    return thisIntent
}

fun Intent.forAppSettings(context: Context): Intent {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = "package:${context.packageName}".toUri()
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
            callIntent.data = "tel:$number".toUri()

            val intent = commonPickShortcutIntent(this, name, callIntent)
            if (photoUri.isNotEmpty()) {
                val resolveProperties = DrawableUri.ResolveProperties(
                    maxIconSize = UtilitiesBitmap.getIconMaxSize(context),
                    targetDensity = UtilitiesBitmap.getTargetDensity(context),
                    context = context
                )
                val d = DrawableUri(context).resolve(photoUri.toUri(), resolveProperties)
                if (d != null) {
                    val bitmap = UtilitiesBitmap.createHiResIconBitmap(d, context)
                    @Suppress("DEPRECATION")
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
                }
            }
            return intent
        }
        cursor.close()
    }
    return null
}