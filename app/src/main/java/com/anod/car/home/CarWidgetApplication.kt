package com.anod.car.home

import android.app.Application
import android.app.UiModeManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioManager
import android.os.DeadSystemException
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.appwidget.WidgetUpdateProvider
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.ModeDetector
import com.anod.car.home.incar.ModeHandler
import com.anod.car.home.incar.ModePhoneStateListener
import com.anod.car.home.notifications.Channels
import com.anod.car.home.utils.AppUpgrade
import com.anod.car.home.utils.WidgetShortcutResource
import com.anod.car.home.utils.permissionDescriptions
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.appwidget.WidgetUpdate
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.di.createAppModule
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation
import info.anodsplace.carwidget.permissions.PermissionChecker
import info.anodsplace.carwidget.skin.SkinPropertiesFactory
import info.anodsplace.carwidget.utils.DefaultsResourceProvider
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.app.ApplicationInstance
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.limiter
import org.acra.config.notification
import org.acra.ktx.initAcra
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

class CarWidgetApplication : Application(), ApplicationInstance, KoinComponent {

    override val appCompatNightMode: Int
        get() =  get<AppSettings>().appCompatNightMode

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        AppLog.tag = "CarWidget"
        if (!AppUpgrade.isUpgraded(this)) {
            initCrashReporter()
        }
    }

    private fun initCrashReporter() {
        val androidCrashHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
        ACRA.DEV_LOGGING = BuildConfig.DEBUG
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportSendSuccessToast = getString(info.anodsplace.carwidget.content.R.string.crash_dialog_toast)
            sendReportsInDevMode = true
            reportContent = listOf(
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.USER_APP_START_DATE,
                ReportField.USER_CRASH_DATE,
                ReportField.REPORT_ID,
                ReportField.PHONE_MODEL,
                ReportField.BRAND,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT
            )
            logcatArguments = listOf("-t", "100", "-v", "brief", "CarWidget:D", "*:S")
            notification {
                channelName = getString(info.anodsplace.carwidget.content.R.string.channel_crash_reports)
                text = getString(info.anodsplace.carwidget.content.R.string.crash_dialog_text)
                title = getString(info.anodsplace.carwidget.content.R.string.crash_dialog_title)
                sendButtonText = getString(info.anodsplace.carwidget.content.R.string.crash_dialog_report_button)
            }
            limiter {
                enabled = !BuildConfig.DEBUG
                overallLimit = 3
                exceptionClassLimit = 1
                failedReportLimit = 1
                stacktraceLimit = 1
            }
        }
        val acraCrashHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            when {
                // Not sure what is going here
                exception is DeadSystemException -> androidCrashHandler.uncaughtException(
                    thread,
                    exception
                )
                // Bug in Android 7.1.1
                exception.message?.contains("startForegroundService") == true -> androidCrashHandler.uncaughtException(
                    thread,
                    exception
                )
                exception.message?.contains("system server dead") == true -> androidCrashHandler.uncaughtException(
                    thread,
                    exception
                )
                else -> acraCrashHandler.uncaughtException(thread, exception)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLog.tag = "CarWidget"
        AppLog.setDebug(BuildConfig.DEBUG, "CarWidget")

        startKoin {
            koin.loadModules(
                modules = listOf(
                    createAppModule(),
                    module {
                        single<Context> { this@CarWidgetApplication }
                        single<Application> { this@CarWidgetApplication }
                        single<WidgetIds> { WidgetHelper(context = get()) }
                        factory { createInCarStatus() } bind InCarStatus::class

                        factory { get<Context>().getSystemService(UiModeManager::class.java) }
                        factory { get<Context>().getSystemService(BluetoothManager::class.java) }
                        factory { get<Context>().getSystemService(AudioManager::class.java) }
                        factory { get<Context>().getSystemService(WindowManager::class.java) }
                        factory { get<Context>().getSystemService(TelephonyManager::class.java) }

                        factory<WidgetSettings.DefaultsProvider> { DefaultsResourceProvider(get<Context>()) }
                        factory<ShortcutResources> { WidgetShortcutResource() }
                        factoryOf(::WidgetUpdateProvider) bind WidgetUpdate::class
                        factoryOf(::SkinPropertiesFactory) bind SkinProperties.Factory::class
                        factory { (skinName: String) -> get<SkinPropertiesFactory>().create(skinName) }
                        factoryOf(::ScreenOrientation)
                        factoryOf(::AlertWindow)
                        singleOf(::ScreenOnAlert)
                        factory {
                            BroadcastService.Manager(applicationContext = get(), inCarSettings = get())
                        } bind BroadcastServiceManager::class
                        singleOf(::ModeHandler)
                        factoryOf(::ModePhoneStateListener)
                        factoryOf(::PermissionChecker)
                        factory(named("permissionDescriptions")) { permissionDescriptions }
                    },
                    createWidgetInstanceModule(),
                ),
            )
        }

        AppCompatDelegate.setDefaultNightMode(appCompatNightMode)
        Channels.register(this)
    }

    private fun createInCarStatus(): InCarStatus {
        val prefs: InCarInterface = get()
        return info.anodsplace.carwidget.incar.InCarStatus(
            widgetIds = get(),
            modeEventsState = { ModeDetector.eventsState() },
            settings = prefs
        )
    }
}

fun Context.getKoin(): Koin {
    return (applicationContext as CarWidgetApplication).getKoin()
}