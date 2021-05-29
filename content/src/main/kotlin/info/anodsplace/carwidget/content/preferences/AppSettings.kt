package info.anodsplace.carwidget.content.preferences

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * @author algavris
 * @date 19/03/2016.
 */
class AppSettings(context: Context) : ChangeableSharedPreferences(getSharedPreferences(context)) {

    val isDarkTheme: Boolean
        get() = theme == dark

    var musicApp: ComponentName?
        get() {
            val musicAppString = prefs.getString(MUSIC_APP, null)

            return if (musicAppString != null) {
                ComponentName.unflattenFromString(musicAppString)
            } else null
        }
        set(musicApp) = applyChange(MUSIC_APP, musicApp)

    var theme: Int
        get() = prefs.getInt(APP_THEME, gray)
        set(value) = applyChange(APP_THEME, value)

    companion object {
        const val gray = 0
        const val dark = 1

        const val MUSIC_APP = "music-app"
        const val APP_THEME = "app_theme"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

}
