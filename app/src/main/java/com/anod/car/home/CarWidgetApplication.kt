package com.anod.car.home

import android.app.Application
import android.app.UiModeManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioManager
import android.os.DeadSystemException
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.appwidget.WidgetUpdateProvider
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.ModeDetector
import com.anod.car.home.incar.ModeHandler
import com.anod.car.home.incar.ModePhoneStateListener
import com.anod.car.home.notifications.Channels
import com.anod.car.home.prefs.lookandfeel.SkinPreviewIntentFactory
import com.anod.car.home.skin.SkinPropertiesFactory
import com.anod.car.home.utils.AppUpgrade
import com.anod.car.home.utils.WidgetShortcutResource
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.*
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.createAppModule
import info.anodsplace.carwidget.content.di.createWidgetInstanceModule
import info.anodsplace.carwidget.content.extentions.isServiceRunning
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.DummyWidgetShortcutsModel
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.incar.ScreenOnAlert
import info.anodsplace.carwidget.incar.ScreenOrientation
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.app.ApplicationInstance
import org.acra.ReportField
import org.acra.config.limiter
import org.acra.config.notification
import org.acra.ktx.initAcra
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

class CarWidgetApplication : Application(), ApplicationInstance, KoinComponent {

    override val nightMode: Int
        get() = get<AppSettings>().nightMode

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        AppLog.tag = "CarWidget"
        if (!AppUpgrade.isUpgraded(this)) {
            initCrashReporter()
        }
    }

    private fun initCrashReporter() {
        val androidCrashHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportSendSuccessToast = getString(R.string.crash_dialog_toast)
            reportContent = arrayOf(
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
            logcatArguments = arrayOf("-t", "100", "-v", "brief", "CarWidget:D", "*:S")
            notification {
                channelName = getString(R.string.channel_crash_reports)
                text = getString(R.string.crash_dialog_text)
                title = getString(R.string.crash_dialog_title)
                sendButtonText = getString(R.string.crash_dialog_report_button)
            }
            limiter {
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
                    createWidgetInstanceModule(),

                    module {
                        single<Context> { this@CarWidgetApplication }
                        single<Application> { this@CarWidgetApplication }
                        single<WidgetIds> { WidgetHelper(context = get()) }
                        factory { createInCarStatus() } bind InCarStatus::class

                        scope<AppWidgetIdScope> {
                            factory<WidgetView> { (
                                                      bitmapMemoryCache: BitmapLruCache?,
                                                      pendingIntentFactory: PendingIntentFactory,
                                                      widgetButtonAlternativeHidden: Boolean,
                                                      overrideSkin: String?
                                                  ) ->
                                WidgetViewBuilder(
                                    context = get(),
                                    iconLoader = get(),
                                    appWidgetId = get(),
                                    bitmapMemoryCache = bitmapMemoryCache,
                                    pendingIntentFactory = pendingIntentFactory,
                                    widgetButtonAlternativeHidden = widgetButtonAlternativeHidden,
                                    overrideSkin = overrideSkin,
                                    widgetSettings = get(),
                                    inCarSettings = get(),
                                    shortcutsModel = get(),
                                    koin = getKoin()
                                )
                            }
                            factory<DummyWidgetView> {
                                    (
                                        bitmapMemoryCache: BitmapLruCache?,
                                        pendingIntentFactory: PendingIntentFactory,
                                        widgetButtonAlternativeHidden: Boolean,
                                        overrideSkin: String?
                                    ) ->
                                WidgetViewBuilder(
                                    context = get(),
                                    iconLoader = get(),
                                    appWidgetId = AppWidgetIdScope.previewId,
                                    widgetSettings = WidgetInterface.NoOp(),
                                    inCarSettings = InCarInterface.NoOp(),
                                    koin = getKoin(),
                                    shortcutsModel = DummyWidgetShortcutsModel(context = get()),
                                    bitmapMemoryCache = bitmapMemoryCache,
                                    pendingIntentFactory = pendingIntentFactory,
                                    widgetButtonAlternativeHidden = widgetButtonAlternativeHidden,
                                    overrideSkin = overrideSkin,
                                )
                            }
                            factoryOf(::SkinPreviewIntentFactory) bind PreviewPendingIntentFactory::class
                        }


                        factory { get<Context>().getSystemService(UiModeManager::class.java) }
                        factory { get<Context>().getSystemService(BluetoothManager::class.java) }
                        factory { get<Context>().getSystemService(AudioManager::class.java) }
                        factory { get<Context>().getSystemService(WindowManager::class.java) }

                        factory<WidgetSettings.DefaultsProvider> { DefaultsResourceProvider(get<Context>()) }
                        factory<ShortcutResources> { WidgetShortcutResource() }
                        factoryOf(::WidgetUpdateProvider) bind WidgetUpdate::class
                        factory { (skinName: String) -> SkinPropertiesFactory.create(skinName) }
                        factoryOf(::ScreenOrientation)
                        factoryOf(::AlertWindow)
                        factoryOf(::ScreenOnAlert)
                        factory {
                            ModeHandler(
                                context = get(),
                                screenOrientation = get(),
                                koin = getKoin()
                            )
                        }
                        factoryOf(::ModePhoneStateListener)
                    }),
            )
        }

        AppCompatDelegate.setDefaultNightMode(nightMode)
        Channels.register(this)
    }

    private fun createInCarStatus(): InCarStatus {
        val prefs: InCarInterface = get()
        val context: Context = get()
        return info.anodsplace.carwidget.incar.InCarStatus(
            widgetIds = get(),
            version = get(),
            serviceRequired = { BroadcastService.isServiceRequired(prefs) },
            serviceRunning = { context.isServiceRunning(BroadcastService::class.java) },
            modeEventsState = { ModeDetector.eventsState() },
            settings = prefs
        )
    }
}

fun Context.getKoin(): Koin {
    return (applicationContext as CarWidgetApplication).getKoin()
}