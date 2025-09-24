package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.ktx.equalsHash
import info.anodsplace.ktx.hashCodeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractShortcuts(internal val context: Context, protected val shortcutsDatabase: ShortcutsDatabase) : Shortcuts {
    private var isInitialized = false
    private var _shortcuts: MutableMap<Int, Shortcut?> = mutableMapOf()
    abstract val targetId: Int
    override val shortcuts: Map<Int, Shortcut?>
        get() {
            require(isInitialized)
            return _shortcuts
        }

    override fun equals(other: Any?): Boolean = equalsHash(this, other)

    override fun hashCode(): Int = hashCodeOf(_shortcuts.hashCode())

    abstract suspend fun runDbMigration()

    abstract fun isMigrated(): Boolean

    override suspend fun lazyInit() {
        if (!isInitialized) {
            init()
        }
    }

    override suspend fun init() {
        isInitialized = true
        _shortcuts.clear()
        if (!isMigrated()) {
            runDbMigration()
        }
        val size = count
        _shortcuts = shortcutsDatabase.loadTarget(targetId)
            .filterKeys { it < size }
            .toMutableMap()
        for (i in 0 until size) {
            if (!_shortcuts.containsKey(i)) {
                _shortcuts[i] = null
            }
        }
    }

    override fun get(position: Int): Shortcut? {
        require(isInitialized)
        return shortcuts.getOrDefault(position, null)
    }

    override suspend fun reloadShortcut(position: Int, shortcutId: Long) {
        lazyInit()
        if (shortcutId == Shortcut.ID_UNKNOWN) {
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
        shortcutsDatabase.moveShortcut(targetId, from, to)
    }

    override suspend fun saveIntent(position: Int, intent: ShortcutIntent): CreateShortcutResult = withContext(Dispatchers.IO) {
        lazyInit()
        val createResult = ShortcutInfoFactory.createShortcut(context, position, intent)
        if (createResult is CreateShortcutResult.CreateShortcutResultSuccess) {
            save(position, createResult.info, createResult.icon)
        }
        return@withContext createResult
    }

    override suspend fun saveFolder(
        position: Int,
        intent: ShortcutIntent,
        items: List<ShortcutIntent>
    ): CreateShortcutResult = withContext(Dispatchers.IO) {
        lazyInit()
        val createResult = ShortcutInfoFactory.createShortcut(context, position, intent)
        if (createResult is CreateShortcutResult.CreateShortcutResultSuccess) {
            val itemShortcuts = items.mapNotNull { intent ->
                val itemResult = ShortcutInfoFactory.createShortcut(context, position, intent)
                if (itemResult is CreateShortcutResult.CreateShortcutResultSuccess) {
                    Pair(itemResult.info, itemResult.icon)
                } else {
                    null
                }
            }
            shortcutsDatabase.saveFolder(targetId,
                position = position,
                item = createResult.info,
                icon = createResult.icon,
                items = itemShortcuts
            )
        }
        return@withContext createResult
    }

    override suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {
        lazyInit()
        if (shortcut == null) {
            _shortcuts[position] = null
            shortcutsDatabase.deleteTargetPosition(targetId, position)
        } else {
            shortcutsDatabase.addItem(targetId, position, shortcut, icon!!)
        }
    }

    override suspend fun drop(position: Int) {
        lazyInit()
        shortcutsDatabase.deleteTargetPosition(targetId, position)
    }
}