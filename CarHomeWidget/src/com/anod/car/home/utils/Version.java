package com.anod.car.home.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Version {
	private static final int MAX_TRIAL_TIMES = 10;
	private static final String PREF_TRIAL_TIMES = "trial-times";
	private boolean mIsFree;
	private Context mContext;
	
	private int mTrialCounterCache = -1;
	
	
	private static final String FREE_PACKAGE_NAME = "com.anod.car.home.free";
	public static final String PRO_PACKAGE_NAME = "com.anod.car.home.pro";

	
	public static boolean isFreeVersion(String packageName) {
		return FREE_PACKAGE_NAME.equals(packageName);
	}
	
	public Version(Context context) {
		mIsFree = isFreeVersion(context.getPackageName());
		mContext = context;
	}

	public int getTrialTimesLeft() {
		if (mTrialCounterCache == -1) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			mTrialCounterCache = prefs.getInt(PREF_TRIAL_TIMES, 0);
		}
		return MAX_TRIAL_TIMES - mTrialCounterCache;
	}
	
	public boolean isTrialExpired() {
		return getTrialTimesLeft() <= 0;
	}
	public boolean isFreeAndTrialExpired() {
		return mIsFree && isTrialExpired();
	}
	
	public boolean isFree() {
		return mIsFree;
	}
	
	public boolean isProOrTrial() {
		if (!mIsFree) {
			return true;
		}
		return !isTrialExpired();
	}
	
	
	
	
}
