package com.anod.car.home.appscache;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;

import java.util.ArrayList;
import java.util.List;

public class AppsCacheAsyncTask extends AsyncTask<Integer, Object, Object> {
	final private Callback mCallback;
	final private AppsListCache mAppsListCache;
	final private Context mContext;

	public interface Callback {
		void onIntentFilterInit(Intent intent);
		void onResult(ArrayList<CacheEntry> cacheEntries);
	}
	
	
	public AppsCacheAsyncTask(Context context, AppsListCache appsListCache, Callback callback) {
		mContext = context;
		mCallback = callback;
		mAppsListCache = appsListCache;
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		mCallback.onResult(mAppsListCache.getCacheEntries());
	}

	protected Object doInBackground(Integer... param) {
		loadAllAppsToCache();
		return null;
	}

	private void loadAllAppsToCache() {
		mAppsListCache.flush();
		final Intent mainIntent = new Intent();
		mCallback.onIntentFilterInit(mainIntent);

		final PackageManager packageManager = mContext.getPackageManager();
		List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
		String selfPackage = mContext.getPackageName();
		for (ResolveInfo appInfo : apps) {

			if (!appInfo.activityInfo.packageName.startsWith(selfPackage)) {
				mAppsListCache.put(appInfo);
			}
		}
		mAppsListCache.sort();
	}
}