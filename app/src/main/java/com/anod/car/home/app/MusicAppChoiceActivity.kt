package com.anod.car.home.app

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.anod.car.home.BuildConfig
import com.anod.car.home.databinding.ListFooterMusicAppChoiceBinding
import com.anod.car.home.model.AppsList
import com.anod.car.home.utils.MusicUtils

/**
 * @author alex
 * @date 2014-09-03
 */
class MusicAppChoiceActivity : MusicAppsActivity() {

    private lateinit var binding: ListFooterMusicAppChoiceBinding

    override fun inflateFooterView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
       binding = ListFooterMusicAppChoiceBinding.inflate(layoutInflater, parent, false)
       return binding.root
    }

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
        val musicCmp = entry.componentName!!

        val isRunning = isMusicCmpRunning(musicCmp)

        MusicUtils.sendKeyEventComponent(
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, this, musicCmp, !isRunning
        )

        if (binding.defaultApp.isChecked) {
            val appSettings = App.provide(this).appSettings
            appSettings.musicApp = musicCmp
            appSettings.apply()
        }

        finish()
    }

    private fun isMusicCmpRunning(musicCmp: ComponentName): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses ?: return false
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
