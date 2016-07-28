package com.anod.car.home;

import android.app.Application;

import com.anod.car.home.acra.BrowserUrlSender;
import com.anod.car.home.acra.CrashDialog;
import com.anod.car.home.prefs.model.AppSettings;
import com.anod.car.home.prefs.model.AppTheme;
import com.anod.car.home.utils.AppLog;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
    mode = ReportingInteractionMode.DIALOG,
    resDialogText = R.string.crash_dialog_text,
    resDialogOkToast = R.string.crash_dialog_toast,
    reportDialogClass = CrashDialog.class,
    customReportContent = {
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
public class CarWidgetApplication extends Application {

    private int mThemeIdx;

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        //LeakCanary.install(this);

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