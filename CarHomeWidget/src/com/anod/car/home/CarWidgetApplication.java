package com.anod.car.home;

import com.anod.car.home.model.AppsListCache;

import android.app.Application;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "", // will not be used
		mailTo = "alex.gavrishev@gmail.com",
		customReportContent = {
				ReportField.APP_VERSION_CODE,
				ReportField.APP_VERSION_NAME,
				ReportField.ANDROID_VERSION,
				ReportField.PHONE_MODEL,
				ReportField.CUSTOM_DATA,
				ReportField.STACK_TRACE,
				ReportField.LOGCAT
		},
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_toast_text)
public class CarWidgetApplication extends Application {
	public AppsListCache mAppListCache;
	public AppsListCache mIconThemesCache;

	public AppsListCache getAppListCache() {
		return mAppListCache;
	}

	public void initAppListCache() {
		if (mAppListCache == null) {
			mAppListCache = new AppsListCache(this);
		}
	}

	public AppsListCache getIconThemesCache() {
		return mIconThemesCache;
	}

	public void initIconThemesCache() {
		if (mIconThemesCache == null) {
			mIconThemesCache = new AppsListCache(this);
		}
	}

}
