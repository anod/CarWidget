package com.anod.car.home.model

import android.content.Context
import android.content.Intent
import android.util.SparseArray

import java.util.ArrayList

abstract class AbstractShortcuts(internal val context: Context) : Shortcuts {

    override val shortcuts: SparseArray<Shortcut?> = SparseArray()

    val shortcutModel: ShortcutModel = ShortcutModel(context)

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
                info = shortcutModel.loadShortcut(shortcutId)
            }
            shortcuts.put(cellId, info)
        }
    }

    override fun get(position: Int): Shortcut? {
        return shortcuts.get(position)
    }

    override fun reloadShortcut(position: Int, shortcutId: Long) {
        if (shortcutId == Shortcut.idUnknown) {
            shortcuts.put(position, null)
        } else {
            val info = shortcutModel.loadShortcut(shortcutId)
            shortcuts.put(position, info)
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

    override fun saveIntent(position: Int, data: Intent, isApplicationShortcut: Boolean): Shortcut? {
        val shortcut = ShortcutInfoUtils.createShortcut(context, data, isApplicationShortcut)
        save(position, shortcut.info, shortcut.icon)
        return shortcuts.get(position)
    }

    override fun save(position: Int, shortcut: Shortcut?, icon: ShortcutIcon?) {
        if (shortcut == null) {
            shortcuts.put(position, null)
        } else {
            val id = shortcutModel.addItemToDatabase(context, shortcut, icon!!)
            if (id == Shortcut.idUnknown) {
                shortcuts.put(position, null)
            } else {
                val newInfo = Shortcut(id, shortcut)
                shortcuts.put(position, newInfo)
                saveId(position, id)
            }
        }
    }

    override fun drop(position: Int) {
        val info = shortcuts.get(position)
        if (info != null) {
            shortcutModel.deleteItemFromDatabase(info.id)
            shortcuts.put(position, null)
            dropId(position)
        }
    }

    override fun loadIcon(id: Long): ShortcutIcon {
        return shortcutModel.loadShortcutIcon(id)!!
    }
}
