package com.anod.car.home

import android.app.Application
import android.app.NotificationManager
import android.app.UiModeManager
import android.content.Context
import android.os.DeadSystemException
import android.util.LruCache
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.appwidget.WidgetUpdateProvider
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.ModeDetector
import com.anod.car.home.notifications.Channels
import com.anod.car.home.prefs.lookandfeel.SkinPreviewIntentFactory
import com.anod.car.home.utils.AppUpgrade
import com.anod.car.home.utils.WidgetShortcutResource
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.appwidget.WidgetUpdate
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.createAppModule
import info.anodsplace.carwidget.content.extentions.isServiceRunning
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.app.ApplicationInstance
import org.acra.ReportField
import org.acra.config.limiter
import org.acra.config.notification
import org.acra.ktx.initAcra
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

class CarWidgetApplication : Application(), ApplicationInstance, KoinComponent {

    lateinit var appComponent: AppComponent
    override val notificationManager: NotificationManager
        get() = appComponent.notificationManager
    override val memoryCache: LruCache<String, Any?>
        get() = appComponent.memoryCache

    override val nightMode: Int
        get() = appComponent.appSettings.nightMode

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
            logcatArguments = arrayOf("-t", "100","-v","brief", "CarWidget:D", "*:S")
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
                exception is DeadSystemException -> androidCrashHandler.uncaughtException(thread, exception)
                // Bug in Android 7.1.1
                exception.message?.contains("startForegroundService") == true -> androidCrashHandler.uncaughtException(thread, exception)
                exception.message?.contains("system server dead") == true -> androidCrashHandler.uncaughtException(thread, exception)
                else -> acraCrashHandler.uncaughtException(thread, exception)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLog.tag = "CarWidget"
        AppLog.setDebug(BuildConfig.DEBUG, "CarWidget")

        startKoin {
            koin.loadModules(modules = listOf(module {
                single<Context> { this@CarWidgetApplication } bind Application::class
                single<WidgetIds> { WidgetHelper(get()) }
                factory<InCarStatus> { createInCarStatus() } bind info.anodsplace.carwidget.incar.InCarStatus::class
                factory<WidgetView> { params -> WidgetViewBuilder(
                        context = get(),
                        database = get(),
                        appWidgetId = params[0],
                        bitmapMemoryCache = params[1],
                        pendingIntentFactory = params[2],
                        widgetButtonAlternativeHidden = params[3]
                    )
                }
                factory<PreviewPendingIntentFactory> { params -> SkinPreviewIntentFactory(params.get(), params.get(), get()) }
                factory<WidgetInterface> { params -> WidgetStorage.load(get(), DefaultsResourceProvider(get<Context>()), params.get()) }
                factory<UiModeManager> { get<Context>().getSystemService(UiModeManager::class.java) }
                factory<ShortcutResources> { WidgetShortcutResource() }
                factory<WidgetUpdate> { WidgetUpdateProvider(get()) }
            }))
            modules(modules = createAppModule())
        }

        appComponent = AppComponent(this)
        AppCompatDelegate.setDefaultNightMode(nightMode)

        Channels.register(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppLog.w("Level: $level", tag = "onTrimMemory")
    }

    private fun createInCarStatus(): info.anodsplace.carwidget.incar.InCarStatus {
        val prefs: InCarInterface = get()
        val context: Context = get()
        return info.anodsplace.carwidget.incar.InCarStatus(
            widgetIds = get(), version = get(),
            serviceRequired = { BroadcastService.isServiceRequired(prefs) },
            serviceRunning = { context.isServiceRunning(BroadcastService::class.java) },
            modeEventsState = { ModeDetector.eventsState() },
            settings = prefs
        )
    }
}