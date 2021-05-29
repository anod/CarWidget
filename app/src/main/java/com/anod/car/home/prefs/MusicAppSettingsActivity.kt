package com.anod.car.home.prefs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.app.AppsListActivity
import com.anod.car.home.databinding.ListFooterMusicAppSettingsBinding
import info.anodsplace.carwidget.chooser.AppsListViewModel
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.MediaListLoader
import java.util.*

/**
 * @author alex
 * @date 2014-09-03
 */
class MusicAppSettingsActivity : AppsListActivity() {

    override fun viewModelFactory(): AppsListViewModel.Factory {
        return AppsListViewModel.Factory(App.get(applicationContext), MediaListLoader(applicationContext))
    }

    override val headEntries: List<ChooserEntry>
        get() = listOf(
                ChooserEntry(null, getString(R.string.show_choice), R.drawable.ic_action_list)
            )

    override val isShowTitle = true

    override fun inflateFooterView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        val binding = ListFooterMusicAppSettingsBinding.inflate(layoutInflater, parent, false)
        return binding.root
    }

    override fun onEntryClick(position: Int, entry: ChooserEntry) {
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
