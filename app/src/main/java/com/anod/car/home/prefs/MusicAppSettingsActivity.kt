package com.anod.car.home.prefs

import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.app.MusicAppsActivity
import com.anod.car.home.model.AppsList
import java.util.*

/**
 * @author alex
 * @date 2014-09-03
 */
class MusicAppSettingsActivity : MusicAppsActivity() {

    override val headEntries: List<AppsList.Entry>
        get() {
            val head = ArrayList<AppsList.Entry>(1)
            val none = AppsList.Entry(null, R.drawable.ic_action_list, getString(R.string.show_choice))
            head.add(none)
            return head
        }

    override val isShowTitle: Boolean
        get() = true

    override val footerViewId: Int
        get() = R.layout.list_footer_music_app_settings

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
        val appSettings = App.provide(this).appSettings
        if (position == 0) {
            appSettings.musicApp = null
        } else {
            appSettings.musicApp = entry.componentName
        }
        appSettings.apply()
        finish()
    }
}
