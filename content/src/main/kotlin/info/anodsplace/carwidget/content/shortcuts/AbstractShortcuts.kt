package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import java.util.*

abstract class AbstractShortcuts(internal val context: Context) : Shortcuts {

    override val shortcuts: MutableMap<Int, Shortcut?> = mutableMapOf()

    val shortcutsDatabase: ShortcutsDatabase = ShortcutsDatabase(context)

    protected abstract fun loadCount()

    protected abstract fun saveId(position: Int, shortcutId: Long)

    protected abstract fun dropId(position: Int)

    protected abstract fun loadIds(): ArrayList<Long>

    override fun init() {
        loadCount()
        shortcuts.clear()
        val currentShortcutIds = loadIds()
        for (cellId in 0 until count) {
            val shortcutId = currentShortcutIds[cellId]
            var info: Shortcut? = null
            if (shortcutId != Shortcut.idUnknown) {
                info = shortcutsDatabase.loadShortcut(shortcutId)
            }
            shortcuts[cellId] = info
        }
    }

    override fun get(position: Int): Shortcut? {
        return shortcuts[position]
    }

    override fun reloadShortcut(position: Int, shortcutId: Long) {
        if (shortcutId == Shortcut.idUnknown) {
            shortcuts[position] = null
        } else {
            val info = shortcutsDatabase.loadShortcut(shortcutId)
            shortcuts[position] = info
        }
    }

    override fun move(from: Int, to: Int) {
        if (from == to) {
            return
        }
        val currentShortcutIds = loadIds()
        val srcShortcutId = currentShortcutIds[from]
        val dstShortcutId = currentShortcutIds[to]

        saveId(from, dstShortcutId)
        saveId(to, srcShortcutId)

    }

    override fun saveIntent(position: Int, data: Intent, isApplicationShortcut: Boolean): Pair<Shortcut?, Int> {
        val shortcut = ShortcutInfoUtils.createShortcut(context, data, isApplicationShortcut)
        save(position, shortcut.info, shortcut.icon)
        return Pair(shortcuts[position], shortcut.result)
    }

    override fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {
        if (shortcut == null) {
            shortcuts[position] = null
        } else {
            val id = shortcutsDatabase.addItemToDatabase(context, shortcut, icon!!)
            if (id == Shortcut.idUnknown) {
                shortcuts[position] = null
            } else {
                val newInfo = Shortcut(id, shortcut)
                shortcuts[position] = newInfo
                saveId(position, id)
            }
        }
    }

    override fun drop(position: Int) {
        val info = shortcuts[position]
        if (info != null) {
            shortcutsDatabase.deleteItemFromDatabase(info.id)
            shortcuts[position] = null
            dropId(position)
        }
    }
}
