package info.anodsplace.carwidget.content.db

import info.anodsplace.carwidget.content.shortcuts.CreateShortcutResult
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent

interface Shortcuts {
    val shortcuts: Map<Int, Shortcut?>
    val count: Int

    suspend fun createDefaultShortcuts()

    suspend fun init()

    suspend fun lazyInit()

    suspend fun reloadShortcut(position: Int, shortcutId: Long)

    fun get(position: Int): Shortcut?

    suspend fun saveIntent(position: Int, intent: ShortcutIntent): CreateShortcutResult

    suspend fun saveFolder(position: Int, intent: ShortcutIntent, items: List<ShortcutIntent>): CreateShortcutResult

    suspend fun updateFolderItems(shortcutId: Long, items: List<ShortcutIntent>)

    suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?)

    suspend fun drop(position: Int)

    suspend fun move(from: Int, to: Int)
}