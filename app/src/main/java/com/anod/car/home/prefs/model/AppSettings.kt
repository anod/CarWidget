package com.anod.car.home.prefs.model

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import info.anodsplace.carwidget.preferences.model.ChangeableSharedPreferences

/**
 * @author algavris
 * @date 19/03/2016.
 */
class AppSettings(context: Context) : ChangeableSharedPreferences(getSharedPreferences(context)) {

    var musicApp: ComponentName?
        get() {
            val musicAppString = prefs.getString(MUSIC_APP, null)

            return if (musicAppString != null) {
                ComponentName.unflattenFromString(musicAppString)
            } else null
        }
        set(musicApp) = putChange(MUSIC_APP, musicApp)

    var theme: Int
        get() = prefs.getInt(APP_THEME, AppTheme.gray)
        set(value) = putChange(APP_THEME, value)

    companion object {
        const val MUSIC_APP = "music-app"
        private const val APP_THEME = "app_theme"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

}
