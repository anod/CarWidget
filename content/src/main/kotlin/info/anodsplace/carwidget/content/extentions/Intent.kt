package info.anodsplace.carwidget.content.extentions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

fun Intent.isAvailable(context: Context): Boolean {
    val packageManager = context.packageManager
    val list = packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
    return list.isNotEmpty()
}