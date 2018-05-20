package com.anod.car.home

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate

import com.anod.car.home.acra.BrowserUrlSender
import com.anod.car.home.acra.CrashDialog
import com.anod.car.home.prefs.model.AppSettings
import com.anod.car.home.prefs.model.AppTheme

import org.acra.ACRA
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog

import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationInstance

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
@AcraDialog(resText = R.string.crash_dialog_text, reportDialogClass = CrashDialog::class)
class CarWidgetApplication : Application(), ApplicationInstance {
    var themeIdx: Int = 0

    var appComponent: AppComponent? = null
        private set

    override val nightMode: Int
        get() {
            return if (themeIdx == AppTheme.THEME_HOLO) {
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

        themeIdx = AppSettings.create(this).theme
        AppCompatDelegate.setDefaultNightMode(nightMode)
        appComponent = AppComponent(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppLog.w("Level: $level")
    }

}