package info.anodsplace.carwidget.content.backup

import android.content.Context
import android.content.Intent
import android.util.JsonReader
import android.util.SparseArray
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconConverter
import info.anodsplace.carwidget.content.db.ShortcutWithFolderItems
import info.anodsplace.carwidget.content.db.ShortcutWithIcon
import java.io.IOException

/**
 * @author algavris
 * @date 08/04/2016.
 */
class ShortcutsJsonReader(context: Context) {
    private val iconConverter = ShortcutIconConverter.Default(context)

    @Throws(IOException::class)
    suspend fun readList(reader: JsonReader): SparseArray<ShortcutWithFolderItems> {
        val shortcuts = SparseArray<ShortcutWithFolderItems>()
        reader.readArray {
            val shortcutWithIcon = readShortcut(reader, hasFolderItems = true)
            shortcuts.put(shortcutWithIcon.first.position, shortcutWithIcon)
        }
        return shortcuts
    }

    @Throws(IOException::class)
    private suspend fun readShortcut(reader: JsonReader, hasFolderItems: Boolean): ShortcutWithFolderItems {
        var pos = -1
        var iconType = 0
        var itemType = 0
        var iconData: ByteArray? = null
        var iconPackageName = ""
        var iconResourceName = ""
        var title: CharSequence = ""
        var isCustomIcon = false
        var intent: Intent? = null
        var folderItems: List<ShortcutWithIcon>? = null

        reader.forEachName { name ->
            when (name) {
                "pos" -> pos = reader.nextInt()
                LauncherSettings.Favorites.ITEM_TYPE -> itemType = reader.nextInt()
                LauncherSettings.Favorites.TITLE -> title = reader.nextString()
                LauncherSettings.Favorites.INTENT -> intent = reader.readIntent()
                LauncherSettings.Favorites.ICON_TYPE -> iconType = reader.nextInt()
                LauncherSettings.Favorites.ICON -> iconData = reader.readIntArrayAsBytes()
                LauncherSettings.Favorites.ICON_PACKAGE -> iconPackageName = reader.nextString()
                LauncherSettings.Favorites.ICON_RESOURCE -> iconResourceName = reader.nextString()
                LauncherSettings.Favorites.IS_CUSTOM_ICON -> isCustomIcon = reader.nextInt() == 1
                "folderItems" -> if (hasFolderItems) {
                    folderItems = mutableListOf()
                    reader.readArray {
                        val folderItem = readShortcut(reader, hasFolderItems = false)
                        folderItems.add(Pair(folderItem.first, folderItem.second))
                    }
                } else {
                    reader.skipValue()
                }
                else -> reader.skipValue()
            }
        }

        val shortcut = Shortcut(
            id = Shortcut.ID_UNKNOWN,
            position = pos,
            itemType = itemType,
            title = title,
            isCustomIcon = isCustomIcon,
            intent = intent ?: Intent()
        )

        val shortcutIcon = iconConverter.convert(
            shortcutId = Shortcut.ID_UNKNOWN,
            itemType = itemType,
            iconType = iconType,
            icon = iconData,
            isCustomIcon = isCustomIcon,
            iconResource = Intent.ShortcutIconResource().apply {
                packageName = iconPackageName
                resourceName = iconResourceName
            }
        )

        return ShortcutWithFolderItems(shortcut, shortcutIcon, folderItems)
    }
}
