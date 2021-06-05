package com.anod.car.home

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.DeadSystemException
import android.util.LruCache
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.acra.BrowserUrlSender
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.ModeDetector
import com.anod.car.home.notifications.Channels
import com.anod.car.home.prefs.model.AppTheme
import com.anod.car.home.utils.AppUpgrade
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.createAppModule
import info.anodsplace.carwidget.content.extentions.isServiceRunning
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.app.ApplicationInstance
import org.acra.ACRA
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraLimiter
import org.acra.annotation.AcraNotification
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module


@AcraCore(
        resReportSendSuccessToast = R.string.crash_dialog_toast,
        reportContent = [
            (ReportField.APP_VERSION_NAME),
            (ReportField.APP_VERSION_CODE),
            (ReportField.ANDROID_VERSION),
            (ReportField.USER_APP_START_DATE),
            (ReportField.USER_CRASH_DATE),
            (ReportField.REPORT_ID),
            (ReportField.PHONE_MODEL),
            (ReportField.BRAND),
            (ReportField.STACK_TRACE),
            (ReportField.LOGCAT)],
        reportSenderFactoryClasses = [(BrowserUrlSender.Factory::class)],
        logcatArguments = ["-t", "100","-v","brief", "AppLog:D", "*:S"])
@AcraNotification(
        resChannelName = R.string.channel_crash_reports,
        resText = R.string.crash_dialog_text,
        resTitle = R.string.crash_dialog_title,
        resSendButtonText = R.string.crash_dialog_report_button)
@AcraLimiter(
        overallLimit = 3,
        exceptionClassLimit = 1,
        failedReportLimit = 1,
        stacktraceLimit = 1
)
class CarWidgetApplication : Application(), ApplicationInstance, KoinComponent {

    lateinit var appComponent: AppComponent
    override val notificationManager: NotificationManager
        get() = appComponent.notificationManager
    override val memoryCache: LruCache<String, Any?>
        get() = appComponent.memoryCache

    override val nightMode: Int
        get() = get(named("NightMode"))

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (!AppUpgrade.isUpgraded(this)) {
            initCrashReporter()
        }
    }

    private fun initCrashReporter() {
        initCrashReporter24()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun initCrashReporter24() {
        val androidCrashHandler = Thread.getDefaultUncaughtExceptionHandler() ?: return
        ACRA.init(this)
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
            }))
            modules(modules = createAppModule())
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        appComponent = AppComponent(this)

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