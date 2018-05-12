package com.anod.car.home.model

import android.content.Context
import android.content.Intent
import android.util.SparseArray

import java.util.ArrayList

abstract class AbstractShortcutsContainerModel(private val context: Context) : ShortcutsContainerModel {

    private var mShortcuts: SparseArray<Shortcut>? = null

    val shortcutModel: ShortcutModel = ShortcutModel(context)

    protected abstract fun loadCount()

    abstract override fun getCount(): Int

    protected abstract fun saveShortcutId(position: Int, shortcutId: Long)

    protected abstract fun dropShortcutId(position: Int)

    protected abstract fun loadShortcutIds(): ArrayList<Long>

    override fun init() {
        loadCount()
        mShortcuts = SparseArray(count)
        val currentShortcutIds = loadShortcutIds()
        for (cellId in 0 until count) {
            val shortcutId = currentShortcutIds[cellId]
            var info: Shortcut? = null
            if (shortcutId != Shortcut.NO_ID.toLong()) {
                info = shortcutModel.loadShortcut(shortcutId)
            }
            mShortcuts!!.put(cellId, info)
        }
    }


    override fun getShortcuts(): SparseArray<Shortcut> {
        return mShortcuts ?: SparseArray()
    }

    override fun getShortcut(position: Int): Shortcut {
        return mShortcuts!!.get(position)
    }

    override fun reloadShortcut(position: Int, shortcutId: Long) {
        if (shortcutId == Shortcut.NO_ID.toLong()) {
            mShortcuts!!.put(position, null)
        } else {
            val info = shortcutModel.loadShortcut(shortcutId)
            mShortcuts!!.put(position, info)
        }
    }

    override fun move(from: Int, to: Int) {
        if (from == to) {
            return
        }
        val currentShortcutIds = loadShortcutIds()
        val srcShortcutId = currentShortcutIds[from]
        val dstShortcutId = currentShortcutIds[to]

        saveShortcutId(from, dstShortcutId)
        saveShortcutId(to, srcShortcutId)

    }

    override fun saveShortcutIntent(position: Int, data: Intent,
                                    isApplicationShortcut: Boolean): Shortcut {
        val shortcut = ShortcutInfoUtils.createShortcut(context, data, isApplicationShortcut)
        saveShortcut(position, shortcut.info, shortcut.icon)
        return mShortcuts!!.get(position)
    }

    override fun saveShortcut(position: Int, info: Shortcut?, icon: ShortcutIcon?) {
        if (info == null) {
            mShortcuts!!.put(position, null)
        } else {
            val id = shortcutModel.addItemToDatabase(context, info, icon!!)
            if (id == Shortcut.NO_ID.toLong()) {
                mShortcuts!!.put(position, null)
            } else {
                val newInfo = Shortcut(id, info)
                mShortcuts!!.put(position, newInfo)
                saveShortcutId(position, id)
            }
        }
    }

    override fun dropShortcut(position: Int) {
        val info = mShortcuts!!.get(position)
        if (info != null) {
            shortcutModel.deleteItemFromDatabase(info.id)
            mShortcuts!!.put(position, null)
            dropShortcutId(position)
        }
    }

    override fun loadIcon(id: Long): ShortcutIcon {
        return shortcutModel.loadShortcutIcon(id)
    }
}
