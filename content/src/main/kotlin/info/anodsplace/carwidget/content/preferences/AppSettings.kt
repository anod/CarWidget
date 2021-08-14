package info.anodsplace.carwidget.content.preferences

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * @author algavris
 * @date 19/03/2016.
 */
class AppSettings(context: Context) : ChangeableSharedPreferences(getSharedPreferences(context)) {

    val nightModeChange: Flow<Int> = changes
        .filter { (key, _) -> key == APP_THEME }
        .map { nightMode }

    val nightMode: Int
        get() = when (theme) {
            system -> UiModeManager.MODE_NIGHT_AUTO
            dark -> UiModeManager.MODE_NIGHT_YES
            light -> UiModeManager.MODE_NIGHT_NO
            else -> UiModeManager.MODE_NIGHT_AUTO
        }

    var musicApp: ComponentName?
        get() {
            val musicAppString = prefs.getString(MUSIC_APP, null)

            return if (musicAppString != null) {
                ComponentName.unflattenFromString(musicAppString)
            } else null
        }
        set(musicApp) = applyChange(MUSIC_APP, musicApp)

    var theme: Int
        get() = prefs.getInt(APP_THEME, system)
        set(value) = applyChange(APP_THEME, value)

    companion object {
        const val light = 0
        const val dark = 1
        const val system = 2

        const val MUSIC_APP = "music-app"
        const val APP_THEME = "app_theme"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

}
