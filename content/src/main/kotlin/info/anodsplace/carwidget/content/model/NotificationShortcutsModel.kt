package info.anodsplace.carwidget.content.model

import android.content.Context
import android.graphics.Path
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconLoader

import java.util.ArrayList

class NotificationShortcutsModel private constructor(context: Context) : AbstractShortcuts(context) {

    override val count: Int
        get() = info.anodsplace.carwidget.content.preferences.InCarStorage.NOTIFICATION_COMPONENT_NUMBER

    override val iconLoader: ShortcutIconLoader
        get() = ShortcutIconLoader(shortcutsDatabase, Path(), context)

    val filledCount: Int
        get() {
            val ids = this.loadIds()
            var count = 0
            for (i in 0 until info.anodsplace.carwidget.content.preferences.InCarStorage.NOTIFICATION_COMPONENT_NUMBER) {
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
        info.anodsplace.carwidget.content.preferences.InCarStorage.saveNotifShortcut(context, shortcutId, position)

    }

    override fun dropId(position: Int) {
        info.anodsplace.carwidget.content.preferences.InCarStorage.dropNotifShortcut(position, context)
    }

    override fun loadIds(): ArrayList<Long> {
        return info.anodsplace.carwidget.content.preferences.InCarStorage.getNotifComponents(context)
    }

    companion object {

        fun init(context: Context): NotificationShortcutsModel {
            val model = NotificationShortcutsModel(context)
            model.init()
            return model
        }
    }
}
