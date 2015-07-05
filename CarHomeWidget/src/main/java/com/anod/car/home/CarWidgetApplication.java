package com.anod.car.home;

import com.anod.car.home.acra.BrowserUrlSender;
import com.anod.car.home.acra.CrashDialog;
import com.anod.car.home.prefs.preferences.AppTheme;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(
    mailTo="alex.gavrishev@gmail.com",
    mode = ReportingInteractionMode.DIALOG,
    resDialogText = R.string.crash_dialog_text,
    resDialogOkToast = R.string.crash_dialog_toast,
    reportDialogClass = CrashDialog.class
)
public class CarWidgetApplication extends Application {

    private int mThemeIdx;

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        ACRA.init(this);
        BrowserUrlSender yourSender = new BrowserUrlSender();
        ACRA.getErrorReporter().setReportSender(yourSender);
        super.onCreate();

        mThemeIdx = AppTheme.getTheme(this);
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

}