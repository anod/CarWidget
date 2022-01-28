package info.anodsplace.carwidget.content.db

import android.content.Intent
import android.util.SparseArray

interface Shortcuts {
    val shortcuts: Map<Int, Shortcut?>
    val count: Int

    suspend fun createDefaultShortcuts()

    suspend fun init()

    suspend fun reloadShortcut(position: Int, shortcutId: Long)

    fun get(position: Int): Shortcut?

    suspend fun saveIntent(position: Int, data: Intent, isApplicationShortcut: Boolean): Pair<Shortcut?, Int>

    suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?)

    suspend fun drop(position: Int)

    suspend fun move(from: Int, to: Int)

    fun updateCount(count: Int)
}