package com.anod.car.home

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.DeadSystemException
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.acra.BrowserUrlSender
import com.anod.car.home.notifications.Channels
import com.anod.car.home.prefs.model.AppTheme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationInstance
import org.acra.ACRA
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraNotification

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
            (ReportField.USER_COMMENT)],
        reportSenderFactoryClasses = [(BrowserUrlSender.Factory::class)])
@AcraNotification(
        resChannelName = R.string.channel_crash_reports,
        resText = R.string.crash_dialog_text,
        resTitle = R.string.crash_dialog_title,
        resSendButtonText = R.string.crash_dialog_report_button,
        resSendWithCommentButtonText = R.string.crash_dialog_report_comment_button,
        resCommentPrompt = R.string.crash_dialog_comment)
class CarWidgetApplication : Application(), ApplicationInstance {

    val appComponent: AppComponent by lazy { AppComponent(this) }

    override val nightMode: Int
        get() {
            return if (appComponent.appSettings.theme == AppTheme.dark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        initCrashReporter()
    }

    private fun initCrashReporter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initCrashReporter24()
        } else {
            ACRA.init(this)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun initCrashReporter24() {
        val androidCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        ACRA.init(this)
        val acraCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            when {
                // Not sure what is going here
                exception is DeadSystemException -> androidCrashHandler.uncaughtException(thread, exception)
                // Bug in Android 7.1.1
                exception.message?.contains("startForegroundService")?: false -> androidCrashHandler.uncaughtException(thread, exception)
                else -> acraCrashHandler.uncaughtException(thread, exception)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLog.setDebug(BuildConfig.DEBUG, "CarWidget")

        AppCompatDelegate.setDefaultNightMode(nightMode)
        Channels.register(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppLog.w("Level: $level")
    }
}