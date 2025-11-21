package info.anodsplace.carwidget.chooser

import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent
import info.anodsplace.compose.chooser.ChooserEntry

var ChooserEntry.isAppEntry: Boolean
    get() = extras?.getBoolean("isAppEntry") == true
    set(value) {
        ensureExtras().putBoolean("isAppEntry", value)
    }

fun ChooserEntry.toShortcutIntent(): ShortcutIntent {
    return ShortcutIntent(
        data = getIntent(null),
        isApp = isAppEntry
    )
}

var ChooserEntry.sourceShortcutId: Long
    get() = extras?.getLong("sourceShortcutId", -1) ?: -1
    set(value) {
        ensureExtras().putLong("sourceShortcutId", value)
    }
