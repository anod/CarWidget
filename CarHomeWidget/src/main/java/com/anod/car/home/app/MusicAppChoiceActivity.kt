package com.anod.car.home.app

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.view.KeyEvent
import android.widget.CheckBox
import android.widget.Toast
import com.anod.car.home.BuildConfig
import com.anod.car.home.R
import com.anod.car.home.model.AppsList
import com.anod.car.home.utils.MusicUtils

/**
 * @author alex
 * @date 2014-09-03
 */
class MusicAppChoiceActivity : MusicAppsActivity() {

    override val footerViewId: Int
        get() = R.layout.list_footer_music_app_choice

    internal val defaultApp: CheckBox by lazy { findViewById<CheckBox>(R.id.defaultApp) }

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
        val musicCmp = entry.componentName!!

        val isRunning = isMusicCmpRunning(musicCmp)

        MusicUtils.sendKeyEventComponent(
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, this, musicCmp, !isRunning
        )

        if (defaultApp.isChecked) {
            val appSettings = App.provide(this).appSettings
            appSettings.musicApp = musicCmp
            appSettings.apply()
        }

        finish()
    }

    private fun isMusicCmpRunning(musicCmp: ComponentName): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager
                .runningAppProcesses ?: return false
        for (i in procInfos.indices) {
            if (procInfos[i].processName.startsWith(musicCmp.packageName)) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(applicationContext,
                            musicCmp.packageName + " is running", Toast.LENGTH_LONG).show()
                }
                return true
            }
        }
        return false
    }

}
