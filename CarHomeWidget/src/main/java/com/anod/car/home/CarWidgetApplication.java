package com.anod.car.home;

import android.app.Application;
import android.content.Context;

import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.prefs.preferences.AppTheme;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


@ReportsCrashes(formKey = "", // will not be used
	mailTo = "alex.gavrishev@gmail.com",
	mode = ReportingInteractionMode.DIALOG,
	resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
	resDialogText = R.string.crash_dialog_text,
	resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
	resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
	resDialogCommentPrompt = R.string.crash_dialog_comment_prompt // optional. when defined, adds a user text field input with this text resource as a label
)
public class CarWidgetApplication extends Application {
	public AppsListCache mAppListCache;
	public AppsListCache mIconThemesCache;

	private int mThemeIdx;

	public static CarWidgetApplication getApplication(Context context) {
		return (CarWidgetApplication) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);

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
