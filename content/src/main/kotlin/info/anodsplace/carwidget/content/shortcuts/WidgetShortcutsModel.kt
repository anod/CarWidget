package info.anodsplace.carwidget.content.shortcuts

import android.content.ComponentName
import android.content.Context
import android.content.Intent

import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.extentions.isAvailable
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage

class WidgetShortcutsModel(
    context: Context,
    database: ShortcutsDatabase,
    private val defaultsProvider: WidgetSettings.DefaultsProvider,
    private val appWidgetId: Int
) : AbstractShortcuts(context, database) {

    private val widgetSettings: WidgetSettings
        get() = WidgetStorage.load(context, defaultsProvider, appWidgetId)

    override fun loadCount(): Int {
        return widgetSettings.shortcutsNumber
    }

    override suspend fun loadShortcuts(): Map<Int, Shortcut?> {
        return shortcutsDatabase.loadTarget(appWidgetId)
    }

    override suspend fun dropShortcut(position: Int) {
        shortcutsDatabase.deleteTargetPosition(appWidgetId, position)
    }

    override suspend fun saveShortcut(position: Int, shortcut: Shortcut, icon: ShortcutIcon) {
        shortcutsDatabase.addItem(appWidgetId, position, shortcut, icon)
    }

    override suspend fun moveShortcut(from: Int, to: Int) {
        shortcutsDatabase.moveShortcut(appWidgetId, from, to)
    }

    override suspend fun runDbMigration() {
        val ids = WidgetStorage.getMigrateIds(context, appWidgetId)
        shortcutsDatabase.migrateShortcutPosition(appWidgetId, ids)
        WidgetStorage.launcherComponentsMigrated(context, appWidgetId)
    }

    override fun isMigrated(): Boolean {
        return WidgetStorage.isDbMigrated(context, appWidgetId)
    }

    override suspend fun createDefaultShortcuts() {
        init()
        preloadDefaultShortcuts(appWidgetId)
    }

    private suspend fun preloadDefaultShortcuts(appWidgetId: Int) {
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

        var position = 0
        val data = Intent()
        for (i in list.indices) {
            data.component = list[i]
            if (!data.isAvailable(context)) {
                continue
            }
            val shortcut = ShortcutInfoFactory.infoFromApplicationIntent(context, position, data)
            if (shortcut.info != null) {
                AppLog.i("Init shortcut - " + shortcut.info + " for #$appWidgetId")
                save(position, shortcut.info, shortcut.icon)
                position++
            }
            if (position == 5) {
                break
            }
        }
    }
}