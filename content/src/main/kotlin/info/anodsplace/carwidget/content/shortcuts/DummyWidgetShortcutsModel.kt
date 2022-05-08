package info.anodsplace.carwidget.content.shortcuts

import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.Shortcuts

class DummyWidgetShortcutsModel(private val context: Context) : Shortcuts {
    private var _shortcuts = mutableMapOf<Int, Shortcut?>()
    override val shortcuts: Map<Int, Shortcut?> = _shortcuts
    override val count: Int
        get() = _shortcuts.count()

    override suspend fun createDefaultShortcuts() {

    }

    override suspend fun init() {
        if (_shortcuts.isEmpty()) {
            DefaultShortcuts.load(context).forEachIndexed { index, result ->
                _shortcuts[index] = result.info
            }
        }
    }

    override suspend fun reloadShortcut(position: Int, shortcutId: Long) {

    }

    override fun get(position: Int): Shortcut? = _shortcuts.getOrDefault(position, null)

    override suspend fun saveIntent(
        position: Int,
        data: Intent,
        isApplicationShortcut: Boolean
    ): Pair<Shortcut?, CreateShortcutResult> {
        return Pair(null, CreateShortcutResult.None)
    }

    override suspend fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {

    }

    override suspend fun drop(position: Int) {

    }

    override suspend fun move(from: Int, to: Int) {

    }
}