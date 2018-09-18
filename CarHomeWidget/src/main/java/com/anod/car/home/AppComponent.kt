package com.anod.car.home

import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import com.anod.car.home.incar.ModeHandler
import com.anod.car.home.incar.ModePhoneStateListener
import com.anod.car.home.incar.ScreenOrientation
import com.anod.car.home.model.AppsList

import android.content.Context
import android.content.Context.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.collection.SimpleArrayMap
import com.anod.car.home.app.AppIconLoader
import com.anod.car.home.prefs.model.AppSettings
import com.anod.car.home.prefs.model.AppTheme

/**
 * @author alex
 * @date 2014-10-27
 */
class AppComponent(val application: CarWidgetApplication) {
    private var _appListCache: AppsList? = null
    private var _iconThemesCache: AppsList? = null
    private var _appIconLoader: AppIconLoader? = null

    val appSettings = AppSettings(application)
    var theme = AppTheme(appSettings.theme)

    val appWidgetManager: AppWidgetManager
        get() = application.getSystemService(APPWIDGET_SERVICE) as AppWidgetManager

    val windowManager: WindowManager
        get() = application.getSystemService(WINDOW_SERVICE) as WindowManager

    val telephonyManager: TelephonyManager
        get() = application.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

    val audioManager: AudioManager
        get() = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val screenOrientation: ScreenOrientation
        get() = ScreenOrientation(this.application, windowManager)

    val modePhoneStateListener: ModePhoneStateListener
        get() = ModePhoneStateListener(this.application, audioManager)

    val handler: ModeHandler
        get() = ModeHandler(this.application, screenOrientation)

    val notificationManager: NotificationManager
        get() = this.application.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val packageManager: PackageManager
        get() = application.packageManager

    val appListCache: AppsList
        get() {
            if (_appListCache == null) {
                _appListCache = AppsList()
            }
            return _appListCache!!
        }

    val appIconLoader: AppIconLoader
        get() {
            if (_appIconLoader == null) {
                _appIconLoader = AppIconLoader(this.application)
            }
            return _appIconLoader!!
        }

    val iconThemesCache: AppsList
        get() {
            if (_iconThemesCache == null) {
                _iconThemesCache = AppsList()
            }
            return _iconThemesCache!!
        }

    val updatesHistory: SimpleArrayMap<Int, Boolean> = SimpleArrayMap()

    fun cleanAppListCache() {
        if (_appListCache != null) {
            _appListCache!!.flush()
            _appListCache = null
        }
        if (_iconThemesCache != null) {
            _iconThemesCache!!.flush()
            _iconThemesCache = null
        }
        if (_appIconLoader != null) {
            _appIconLoader!!.shutdown()
            _appIconLoader = null
        }
    }

}
