package info.anodsplace.carwidget.chooser

import android.content.Context
import android.net.Uri
import info.anodsplace.carwidget.content.graphics.AppIconFetcher
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent
import info.anodsplace.compose.chooser.ChooserEntry

fun ChooserEntry.iconUri(context: Context): Uri {
    return if (componentName == null) {
        if (iconRes != 0) {
            context.iconUri(iconRes = iconRes)
        } else Uri.EMPTY
    } else Uri.fromParts(
        AppIconFetcher.SCHEME_APPLICATION_ICON,
        componentName!!.flattenToShortString(),
        null
    )
}

fun ChooserEntry.toShortcutIntent(isApp: Boolean): ShortcutIntent {
    return ShortcutIntent(
        data = getIntent(null),
        isApp = isApp
    )
}