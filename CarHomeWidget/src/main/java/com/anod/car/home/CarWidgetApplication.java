package com.anod.car.home;

import android.app.Application;
import android.view.ViewConfiguration;

import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.prefs.preferences.AppTheme;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.lang.reflect.Field;

@ReportsCrashes(formKey = "", // will not be used
	mailTo = "alex.gavrishev@gmail.com",
	mode = ReportingInteractionMode.DIALOG,
	resDialogText = R.string.crash_toast_text
)
public class CarWidgetApplication extends Application {
	public AppsListCache mAppListCache;
	public AppsListCache mIconThemesCache;
	private int mThemeIdx;

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			// The following line triggers the initialization of ACRA
			ACRA.init(this);
		}
		mThemeIdx = AppTheme.getTheme(this);

	}

	public AppsListCache getAppListCache() {
		return mAppListCache;
	}


	public int getThemeIdx() {
		return mThemeIdx;
	}

	public int setThemeIdx(int theme) {
		return mThemeIdx = theme;
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
