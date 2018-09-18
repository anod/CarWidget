package com.anod.car.home

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.anod.car.home.acra.BrowserUrlSender
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.AppSettings
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
        ACRA.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        AppLog.setDebug(BuildConfig.DEBUG, "CarWidget")

        AppCompatDelegate.setDefaultNightMode(nightMode)
        createNotificationChannels()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppLog.w("Level: $level")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(BroadcastService.channelModeDetector, getString(R.string.mode_detector_channel), NotificationManager.IMPORTANCE_LOW)
            val channel2 = NotificationChannel("incar_mode", getString(R.string.incar_mode), NotificationManager.IMPORTANCE_DEFAULT)
            val channel3 = NotificationChannel(getString(R.string.channel_crash_reports), "Crash reports", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(NotificationManager::class.java)!!
            notificationManager.createNotificationChannels(listOf(channel1, channel2, channel3))
        }
    }
}