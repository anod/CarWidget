package info.anodsplace.carwidget.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import info.anodsplace.framework.content.startActivitySafely

private const val DETAIL_MARKET_URL = "market://details?id=%s"

fun Intent.forApplicationDetails(packageName: String): Intent {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.fromParts("package", packageName, null)
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
    return this
}

fun Intent.playStoreDetails(packageName: String): Intent {
    action = Intent.ACTION_VIEW
    data = Uri.parse(DETAIL_MARKET_URL.format(packageName))
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
    return this
}

fun Context.openPlayStoreDetails(packageName: String) {
    startActivitySafely(Intent().playStoreDetails(packageName))
}

fun Context.openDefaultCarDock() {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_CAR_DOCK)
    val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    val packageName = info?.activityInfo?.applicationInfo?.packageName ?: return
    startActivitySafely(Intent().forApplicationDetails(packageName))
}