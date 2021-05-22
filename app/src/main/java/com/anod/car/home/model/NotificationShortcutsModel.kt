package com.anod.car.home.model

import android.content.Context
import android.graphics.Path
import info.anodsplace.carwidget.db.Shortcut
import info.anodsplace.carwidget.db.ShortcutIconLoader

import info.anodsplace.carwidget.preferences.model.InCarStorage

import java.util.ArrayList

class NotificationShortcutsModel private constructor(context: Context) : AbstractShortcuts(context) {

    override val count: Int
        get() = InCarStorage.NOTIFICATION_COMPONENT_NUMBER

    override val iconLoader: ShortcutIconLoader
        get() = ShortcutIconLoader(shortcutsDatabase, Path(), context)

    val filledCount: Int
        get() {
            val ids = this.loadIds()
            var count = 0
            for (i in 0 until InCarStorage.NOTIFICATION_COMPONENT_NUMBER) {
                count += if (ids[i] == Shortcut.idUnknown) 0 else 1
            }
            return count
        }

    override fun createDefaultShortcuts() {
        // Nothing
    }

    override fun loadCount() {
        //nothing
    }

    override fun updateCount(count: Int) {
        // :( Exception
    }

    override fun saveId(position: Int, shortcutId: Long) {
        InCarStorage.saveNotifShortcut(context, shortcutId, position)

    }

    override fun dropId(position: Int) {
        InCarStorage.dropNotifShortcut(position, context)
    }

    override fun loadIds(): ArrayList<Long> {
        return InCarStorage.getNotifComponents(context)
    }

    companion object {

        fun init(context: Context): NotificationShortcutsModel {
            val model = NotificationShortcutsModel(context)
            model.init()
            return model
        }
    }
}
