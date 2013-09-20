package com.anod.car.home;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.os.Build;

import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.utils.Utils;

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

		if (BuildConfig.DEBUG) {
			// The following line triggers the initialization of ACRA
			ACRA.init(this);
		}

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
