package com.anod.car.home.utils

import com.anod.car.home.ShortcutActivity
import com.anod.car.home.app.NewShortcutActivity
import com.anod.car.home.incar.SwitchInCarActivity
import com.anod.car.home.prefs.LookAndFeelActivity

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import info.anodsplace.framework.AppLog

fun Intent.forNewShortcut(context: Context, appWidgetId: Int, cellId: Int): Intent {
    component = ComponentName(context, NewShortcutActivity::class.java)
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    putExtra(ShortcutPicker.EXTRA_CELL_ID, cellId)
    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NEW_TASK)
    val path = appWidgetId.toString() + "/" + cellId
    data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path)
    return this
}

fun Intent.forSettings(context: Context, appWidgetId: Int): Intent {
    component = ComponentName(context, LookAndFeelActivity::class.java)
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),
            appWidgetId.toString())
    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

fun Intent.forApplicationDetails(packageName: String): Intent {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.fromParts("package", packageName, null)
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
    return this
}

fun Intent.isAvailable(context: Context): Boolean {
    val packageManager = context.packageManager
    val list = packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
    return !list.isEmpty()
}

fun Intent.forProVersion(): Intent {
    action = Intent.ACTION_VIEW
    data = Uri.parse(String.format("market://details?id=%s", Version.PRO_PACKAGE_NAME))
    return this
}

fun Intent.forShortcut(context: Context, i: Int): Intent {
    when (i) {
        0 -> component = ComponentName(context, SwitchInCarActivity::class.java)
        1 -> {
            action = Intent.ACTION_PICK
            data = ContactsContract.Contacts.CONTENT_URI
        }
        2 -> fillMediaButtonIntent(this, context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        3 -> fillMediaButtonIntent(this, context, KeyEvent.KEYCODE_MEDIA_NEXT)
        4 -> fillMediaButtonIntent(this, context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }
    return this
}

private fun fillMediaButtonIntent(intent: Intent, context: Context, keyCode: Int) {
    intent.component = ComponentName(context, ShortcutActivity::class.java)
    intent.action = ShortcutActivity.ACTION_MEDIA_BUTTON
    intent.putExtra(ShortcutActivity.EXTRA_MEDIA_BUTTON, keyCode)
}

fun Intent.forPickShortcutLocal(i: Int, title: String, icnResId: Int, ctx: Context): Intent {
    val shortcutIntent = Intent().forShortcut(ctx, i)
    shortcutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY

    val intent = commonPickShortcutIntent(title, shortcutIntent)
    val iconResource = Intent.ShortcutIconResource.fromContext(ctx, icnResId)
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
    return intent
}

private fun commonPickShortcutIntent(title: String, shortcutIntent: Intent): Intent {
    val intent = Intent()
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
    return intent
}

fun Intent.forAppSettings(context: Context): Intent {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.parse("package:${context.packageName}")
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

fun Intent.forDirectCall(contactUri: Uri, context: Context): Intent? {
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
            callIntent.data = Uri.parse("tel:$number")

            val intent = commonPickShortcutIntent(name, callIntent)
            if (!TextUtils.isEmpty(photoUri)) {
                val d = DrawableUri(context).resolve(Uri.parse(photoUri))
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