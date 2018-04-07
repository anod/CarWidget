package com.anod.car.home.prefs

import com.anod.car.home.R
import com.anod.car.home.app.MusicAppsActivity
import com.anod.car.home.model.AppsList
import com.anod.car.home.prefs.model.AppSettings

import java.util.ArrayList

/**
 * @author alex
 * @date 2014-09-03
 */
class MusicAppSettingsActivity : MusicAppsActivity() {

    override fun getHeadEntries(): ArrayList<AppsList.Entry>? {
        val head = ArrayList<AppsList.Entry>(1)
        val none = AppsList.Entry()
        none.iconRes = R.drawable.ic_action_list
        none.title = getString(R.string.show_choice)
        head.add(none)
        return head
    }

    override fun isShowTitle(): Boolean {
        return true
    }

    override fun getFooterViewId(): Int {
        return R.layout.music_app_settings_footer
    }

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
        val appSettings = AppSettings.create(this)
        if (position == 0) {
            appSettings.musicApp = null
        } else {
            appSettings.musicApp = entry.componentName
        }
        appSettings.apply()
        finish()
    }
}
