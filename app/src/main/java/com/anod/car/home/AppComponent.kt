package com.anod.car.home

import android.app.Application
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.telephony.TelephonyManager
import android.util.LruCache
import android.view.WindowManager
import com.anod.car.home.incar.ModeHandler
import com.anod.car.home.incar.ModePhoneStateListener
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation
import info.anodsplace.carwidget.content.preferences.AppSettings
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.util.createLruCache
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * @author alex
 * @date 2014-10-27
 */
class AppComponent(val application: Application) : KoinComponent {
    val shortcutResources: ShortcutResources
        get() = get()

    val appSettings: AppSettings
        get() = get()

    val appWidgetManager: AppWidgetManager
        get() = application.getSystemService(APPWIDGET_SERVICE) as AppWidgetManager

    private val windowManager: WindowManager
        get() = application.getSystemService(WINDOW_SERVICE) as WindowManager

    val telephonyManager: TelephonyManager
        get() = application.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

    private val audioManager: AudioManager
        get() = application.getSystemService(AUDIO_SERVICE) as AudioManager

    private val screenOrientation: ScreenOrientation
        get() = ScreenOrientation(this.application, windowManager)

    val modePhoneStateListener: ModePhoneStateListener
        get() = ModePhoneStateListener(this.application, audioManager)

    val handler: ModeHandler
        get() = ModeHandler(application, screenOrientation)

    val alertWindow: ScreenOnAlert
        get() = ScreenOnAlert(application, get(), AlertWindow(this.application))

    val notificationManager: NotificationManager
        get() = this.application.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val packageManager: PackageManager
        get() = application.packageManager

    val appIconLoader: Picasso
        get() = get()

    val memoryCache: LruCache<String, Any?> by lazy {
        createLruCache()
    }
}
