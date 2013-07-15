package com.anod.car.home.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Version {
	private static final int MAX_TRIAL_TIMES = 30;
	private static final String PREF_TRIAL_TIMES = "trial-times";
	private final boolean mIsFree;
	private final Context mContext;
	
	private int mTrialCounterCache = -1;
	private SharedPreferences mPrefs;
	
	
	private static final String FREE_PACKAGE_NAME = "com.anod.car.home.free";
	public static final String PRO_PACKAGE_NAME = "com.anod.car.home.pro";

	
	public static boolean isFreeVersion(String packageName) {
		return packageName.startsWith(FREE_PACKAGE_NAME);
	}
	
	public Version(Context context) {
		mIsFree = isFreeVersion(context.getPackageName());
		mContext = context;
	}

	public int getMaxTrialTimes() {
		return MAX_TRIAL_TIMES;
	}

	public int getTrialTimesLeft() {
		initTrialCounter();
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

	public void increaseTrialCounter() {
		initTrialCounter();
		mTrialCounterCache++;
		mPrefs.edit().putInt(PREF_TRIAL_TIMES, mTrialCounterCache).commit();
	}
	
	private void initTrialCounter() {
		if (mTrialCounterCache == -1) {
			mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			mTrialCounterCache = mPrefs.getInt(PREF_TRIAL_TIMES, 0);
		}
	}
	
	
}
