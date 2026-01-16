package info.anodsplace.carwidget.content.backup

import android.content.Context
import android.util.JsonWriter
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconConverter
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.db.toShortcutIcon

suspend fun JsonWriter.writeShortcuts(shortcuts: Map<Int, Shortcut?>, db: ShortcutsDatabase, context: Context) {
    val iconConverter = ShortcutIconConverter.Default(context)

    for (pos in shortcuts.keys.sorted()) {
        val shortcut = shortcuts[pos] ?: continue
        val dbIcon = db.loadIcon(shortcut.id)
        val icon = iconConverter.toShortcutIcon(shortcut.id, dbIcon)
        val values = ShortcutsDatabase.createShortcutContentValues(shortcut, icon)

        obj {
            field("pos", pos)
            contentValues(values)

            if (shortcut.isFolder) {
                val folderItems = db.loadFolderItems(shortcut.id)
                array("folderItems") {
                    for (item in folderItems) {
                        val folderDbIcon = db.loadFolderIcon(item.id)
                        val icon = iconConverter.toShortcutIcon(item.id, folderDbIcon)
                        val itemValues = ShortcutsDatabase.createShortcutContentValues(item, icon)
                        obj {
                            contentValues(itemValues)
                        }
                    }
                }
            }
        }
    }
}