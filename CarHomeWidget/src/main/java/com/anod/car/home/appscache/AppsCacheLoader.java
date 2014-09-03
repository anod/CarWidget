package com.anod.car.home.appscache;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.anod.car.home.model.AppsList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @date 2014-09-02
 */
public class AppsCacheLoader extends AsyncTaskLoader<ArrayList<AppsList.Entry>> {
    final private Callback mCallback;
    final private AppsList mAppsList;
    final private Context mContext;

    public AppsCacheLoader(Context context, Callback callback, AppsList appsList) {
        super(context);
        mContext = context;
        mCallback = callback;
        mAppsList = appsList;
    }

    public interface Callback {
        void onIntentFilterInit(Intent intent);
    }

    @Override
    public ArrayList<AppsList.Entry> loadInBackground() {
        loadAllAppsToCache();
        return mAppsList.getEntries();
    }

    private void loadAllAppsToCache() {
        mAppsList.flush();
        final Intent mainIntent = new Intent();
        mCallback.onIntentFilterInit(mainIntent);

        final PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        String selfPackage = mContext.getPackageName();
        for (ResolveInfo appInfo : apps) {

            if (!appInfo.activityInfo.packageName.startsWith(selfPackage)) {
                mAppsList.put(appInfo);
            }
        }
        mAppsList.sort();
    }
}
