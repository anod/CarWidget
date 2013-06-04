package com.anod.car.home;

import com.anod.car.home.model.AppsListCache;

import android.app.Application;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "", // will not be used
		mailTo = "alex.gavrishev@gmail.com",
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_toast_text)
public class CarWidgetApplication extends Application {
	public AppsListCache mAppListCache;
	public AppsListCache mIconThemesCache;

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}

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
