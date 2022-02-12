package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractShortcuts(internal val context: Context, protected val shortcutsDatabase: ShortcutsDatabase) : Shortcuts {
    private var isInitialized = false
    private var _count: Int = 0
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

    abstract suspend fun loadShortcuts(): Map<Int, Shortcut?>

    abstract suspend fun dropShortcut(position: Int)

    abstract suspend fun saveShortcut(position: Int, shortcut: Shortcut, icon: ShortcutIcon)

    abstract suspend fun moveShortcut(from: Int, to: Int)

    abstract suspend fun runDbMigration()

    abstract fun isMigrated(): Boolean

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
        if (!isMigrated()) {
            runDbMigration()
        }
        _shortcuts = loadShortcuts().toMutableMap()
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
        moveShortcut(from, to)
    }

    override suspend fun saveIntent(position: Int, data: Intent, isApplicationShortcut: Boolean): Pair<Shortcut?, CreateShortcutResult> = withContext(Dispatchers.IO) {
        lazyInit()
        val shortcut = ShortcutInfoFactory.createShortcut(context, position, data, isApplicationShortcut)
        save(position, shortcut.info, shortcut.icon)
        return@withContext Pair(shortcuts[position], shortcut.result)
    }

    override suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {
        lazyInit()
        if (shortcut == null) {
            _shortcuts[position] = null
            dropShortcut(position)
        } else {
            saveShortcut(position, shortcut, icon!!)
        }
    }

    override suspend fun drop(position: Int) {
        lazyInit()
        dropShortcut(position)
    }
}
