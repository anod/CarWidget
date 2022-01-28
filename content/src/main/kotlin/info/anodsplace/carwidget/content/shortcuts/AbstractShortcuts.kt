package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import java.util.*

abstract class AbstractShortcuts(internal val context: Context, protected val shortcutsDatabase: ShortcutsDatabase) : Shortcuts {
    private var isInitialized = false
    protected var _count: Int = 0
    private var _shortcuts: MutableMap<Int, Shortcut?> = mutableMapOf()

    override val shortcuts: Map<Int, Shortcut?>
        get() {
            require(isInitialized)
            return _shortcuts
        }

    override val count: Int
        get() {
            require(isInitialized)
            return _count
        }

    protected abstract fun loadCount(): Int

    protected abstract suspend fun saveId(position: Int, shortcutId: Long)

    protected abstract fun dropId(position: Int)

    protected abstract fun loadIds(): ArrayList<Long>

    protected abstract fun countUpdated(count: Int)

    private suspend fun lazyInit() {
        if (!isInitialized) {
            init()
        }
    }

    override suspend fun init() {
        isInitialized = true
        _count = loadCount()
        _shortcuts.clear()
        val currentShortcutIds = loadIds()
        for (cellId in 0 until count) {
            val shortcutId = currentShortcutIds[cellId]
            var info: Shortcut? = null
            if (shortcutId != Shortcut.idUnknown) {
                info = shortcutsDatabase.loadShortcut(shortcutId)
            }
            _shortcuts[cellId] = info
        }
    }

    override fun updateCount(count: Int) {
        require(isInitialized)
        this._count = count
        countUpdated(count)
    }

    override fun get(position: Int): Shortcut? {
        require(isInitialized)
        return shortcuts[position]
    }

    override suspend fun reloadShortcut(position: Int, shortcutId: Long) {
        lazyInit()
        if (shortcutId == Shortcut.idUnknown) {
            _shortcuts[position] = null
        } else {
            val info = shortcutsDatabase.loadShortcut(shortcutId)
            _shortcuts[position] = info
        }
    }

    override suspend fun move(from: Int, to: Int) {
        lazyInit()
        if (from == to) {
            return
        }
        val currentShortcutIds = loadIds()
        val srcShortcutId = currentShortcutIds[from]
        val dstShortcutId = currentShortcutIds[to]

        saveId(from, dstShortcutId)
        saveId(to, srcShortcutId)

    }

    override suspend fun saveIntent(position: Int, data: Intent, isApplicationShortcut: Boolean): Pair<Shortcut?, Int> {
        lazyInit()
        val shortcut = ShortcutInfoUtils.createShortcut(context, data, isApplicationShortcut)
        save(position, shortcut.info, shortcut.icon)
        return Pair(shortcuts[position], shortcut.result)
    }

    override suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {
        lazyInit()
        if (shortcut == null) {
            _shortcuts[position] = null
        } else {
            val id = shortcutsDatabase.addItemToDatabase(shortcut, icon!!)
            if (id == Shortcut.idUnknown) {
                _shortcuts[position] = null
            } else {
                val newInfo = Shortcut(id, shortcut)
                _shortcuts[position] = newInfo
                saveId(position, id)
            }
        }
    }

    override suspend fun drop(position: Int) {
        lazyInit()
        val info = _shortcuts[position]
        if (info != null) {
            shortcutsDatabase.deleteItemFromDatabase(info.id)
            _shortcuts[position] = null
            dropId(position)
        }
    }
}
