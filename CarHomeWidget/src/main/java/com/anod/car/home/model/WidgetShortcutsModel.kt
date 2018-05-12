package com.anod.car.home.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent

import com.anod.car.home.prefs.model.WidgetStorage
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.IntentUtils

import java.util.ArrayList

class WidgetShortcutsModel(private val mContext: Context, private val mAppWidgetId: Int) : AbstractShortcutsContainerModel(mContext) {

    private var count: Int = 0

    public override fun loadCount() {
        count = WidgetStorage.getLaunchComponentNumber(mContext, mAppWidgetId)
    }

    override fun updateCount(count: Int) {
        this.count = count
        WidgetStorage.saveLaunchComponentNumber(count, mContext, mAppWidgetId)
    }

    override fun loadShortcutIds(): ArrayList<Long> {
        loadCount()
        return WidgetStorage.getLauncherComponents(mContext, mAppWidgetId, count)
    }

    override fun saveShortcutId(position: Int, shortcutId: Long) {
        WidgetStorage.saveShortcut(mContext, shortcutId, position, mAppWidgetId)
    }

    override fun dropShortcutId(position: Int) {
        WidgetStorage.dropShortcutPreference(position, mAppWidgetId, mContext)
    }

    override fun getCount(): Int {
        return count
    }

    override fun createDefaultShortcuts() {
        init()
        initShortcuts(mAppWidgetId)
    }

    private fun initShortcuts(appWidgetId: Int) {
        val list = arrayOf(
                ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity"),
                ComponentName("com.android.htccontacts", "com.android.htccontacts.DialerTabActivity"), //HTC Phone
                //
                ComponentName("com.android.music", "com.android.music.MusicBrowserActivity"),
                ComponentName("com.htc.music", "com.htc.music.HtcMusic"),
                ComponentName("com.sec.android.app.music", "com.sec.android.app.music.MusicActionTabActivity"),
                ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"),
                // TODO: add spotify deezer, play music
                ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity"))

        var cellId = 0
        val data = Intent()
        for (i in list.indices) {
            data.component = list[i]
            if (!IntentUtils.isIntentAvailable(mContext, data)) {
                continue
            }
            val shortcut = ShortcutInfoUtils.infoFromApplicationIntent(mContext, data)
            if (shortcut != null) {
                AppLog.d("Init shortcut - " + shortcut.info + " Widget - " + appWidgetId)
                saveShortcut(cellId, shortcut.info, shortcut.icon)
                cellId++
            }
            if (cellId == 5) {
                break
            }
        }

    }

    companion object {
        fun init(context: Context, appWidgetId: Int): WidgetShortcutsModel {
            val model = WidgetShortcutsModel(context, appWidgetId)
            model.init()
            return model
        }
    }

}
