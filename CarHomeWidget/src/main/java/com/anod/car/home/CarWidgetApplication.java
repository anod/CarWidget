package com.anod.car.home;

import android.app.Application;
import android.content.Context;

import com.anod.car.home.acra.BrowserUrlSender;
import com.anod.car.home.acra.CrashDialog;
import com.anod.car.home.prefs.model.AppSettings;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;

import info.anodsplace.android.log.AppLog;


@AcraCore(
    resReportSendSuccessToast = R.string.crash_dialog_toast,
    reportContent = {
            ReportField.APP_VERSION_NAME, ReportField.APP_VERSION_CODE,
            ReportField.ANDROID_VERSION,
            ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE,
            ReportField.REPORT_ID,
            ReportField.PHONE_MODEL,
            ReportField.BRAND,
            ReportField.STACK_TRACE,
            ReportField.USER_COMMENT
    },
    reportSenderFactoryClasses = { BrowserUrlSender.Factory.class }
)
@AcraDialog(
    resText = R.string.crash_dialog_text,
    reportDialogClass = CrashDialog.class
)
public class CarWidgetApplication extends Application {

    private int mThemeIdx;

    private ObjectGraph mObjectGraph;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        AppLog.setDebug(BuildConfig.DEBUG, "CarWidget");


        mThemeIdx = AppSettings.create(this).getTheme();
        mObjectGraph = new ObjectGraph(this);
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    public int getThemeIdx() {
        return mThemeIdx;
    }

    public int setThemeIdx(int theme) {
        return mThemeIdx = theme;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AppLog.w("Level: " + level);
    }

}