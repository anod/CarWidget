package info.anodsplace.carwidget.content.db

import android.content.Intent
import android.util.SparseArray

interface Shortcuts {
    val iconLoader: ShortcutIconLoader
    val shortcuts: Map<Int, Shortcut?>
    val count: Int

    fun createDefaultShortcuts()

    fun init()

    fun reloadShortcut(position: Int, shortcutId: Long)

    fun get(position: Int): Shortcut?

    fun saveIntent(position: Int, data: Intent, isApplicationShortcut: Boolean): Pair<Shortcut?, Int>

    fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?)

    fun drop(position: Int)

    fun move(from: Int, to: Int)

    fun updateCount(count: Int)
}