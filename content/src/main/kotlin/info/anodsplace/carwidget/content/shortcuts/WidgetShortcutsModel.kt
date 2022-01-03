package info.anodsplace.carwidget.content.shortcuts

import android.content.ComponentName
import android.content.Context
import android.content.Intent

import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.extentions.isAvailable
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage

import java.util.ArrayList

class WidgetShortcutsModel(context: Context, database: ShortcutsDatabase, private val defaultsProvider: WidgetSettings.DefaultsProvider, private val appWidgetId: Int) : AbstractShortcuts(context, database) {

    override var count: Int = 0

    override val iconLoader: ShortcutIconLoader
        get() = ShortcutIconLoader(shortcutsDatabase, defaultsProvider, appWidgetId, context)

    private val widgetSettings: WidgetSettings
        get() = WidgetStorage.load(context, defaultsProvider, appWidgetId)

    public override fun loadCount() {
        count = widgetSettings.shortcutsNumber
    }

    override fun updateCount(count: Int) {
        this.count = count
        widgetSettings.shortcutsNumber = count
    }

    override fun loadIds(): ArrayList<Long> {
        loadCount()
        return WidgetStorage.getLauncherComponents(context, appWidgetId, count)
    }

    override fun saveId(position: Int, shortcutId: Long) {
        WidgetStorage.saveShortcut(shortcutsDatabase, context, shortcutId, position, appWidgetId)
    }

    override fun dropId(position: Int) {
        WidgetStorage.dropShortcutPreference(position, appWidgetId, context)
    }

    override fun createDefaultShortcuts() {
        init()
        initShortcuts(appWidgetId)
    }

    private fun initShortcuts(appWidgetId: Int) {
        val list = arrayOf(
                ComponentName("com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity"),
                ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity"),
                ComponentName("com.android.htccontacts", "com.android.htccontacts.DialerTabActivity"), //HTC CallPhone
                //
                ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"),
                //
                ComponentName("com.google.android.music", "com.android.music.activitymanagement.TopLevelActivity"),
                ComponentName("com.htc.music", "com.htc.music.HtcMusic"),
                ComponentName("com.sec.android.app.music", "com.sec.android.app.music.MusicActionTabActivity"),
                ComponentName("com.spotify.music", "com.spotify.music.MainActivity"),
                ComponentName("tunein.player", "tunein.ui.actvities.TuneInHomeActivity"),

                ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity"))

        var cellId = 0
        val data = Intent()
        for (i in list.indices) {
            data.component = list[i]
            if (!data.isAvailable(context)) {
                continue
            }
            val shortcut = ShortcutInfoUtils.infoFromApplicationIntent(context, data)
            if (shortcut.info != null) {
                AppLog.i("Init shortcut - " + shortcut.info + " for #$appWidgetId")
                save(cellId, shortcut.info, shortcut.icon)
                cellId++
            }
            if (cellId == 5) {
                break
            }
        }

    }

    companion object {
        fun init(context: Context, db: ShortcutsDatabase, defaultsProvider: WidgetSettings.DefaultsProvider, appWidgetId: Int): WidgetShortcutsModel {
            return WidgetShortcutsModel(context, db, defaultsProvider, appWidgetId).apply {
                init()
            }
        }
    }

}
