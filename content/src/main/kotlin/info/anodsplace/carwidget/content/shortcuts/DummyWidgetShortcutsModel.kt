package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.Shortcuts

class DummyWidgetShortcutsModel(private val context: Context, size: Int) : Shortcuts {
    private var _shortcuts = mutableMapOf<Int, Shortcut?>().also { map ->
        (0 until size).forEach { index ->
            map[index] = null
        }
    }
    override val shortcuts: Map<Int, Shortcut?> = _shortcuts
    override val count: Int = size

    override suspend fun createDefaultShortcuts() {
        DefaultShortcuts.load(context).forEachIndexed { index, shortcut ->
            _shortcuts[index] = shortcut.info
        }
    }

    override suspend fun init() {

    }

    override suspend fun lazyInit() {

    }

    override suspend fun reloadShortcut(position: Int, shortcutId: Long) {

    }

    override fun get(position: Int): Shortcut? = _shortcuts.getOrDefault(position, null)

    override suspend fun saveIntent(
        position: Int,
        intent: ShortcutIntent,
    ): CreateShortcutResult {
        return CreateShortcutResult.None
    }

    override suspend fun saveFolder(
        position: Int,
        intent: ShortcutIntent,
        items: List<ShortcutIntent>
    ): CreateShortcutResult {
        return CreateShortcutResult.None
    }

    override suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {

    }

    override suspend fun drop(position: Int) {

    }

    override suspend fun move(from: Int, to: Int) {

    }
}