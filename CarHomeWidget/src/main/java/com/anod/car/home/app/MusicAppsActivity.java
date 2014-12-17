package com.anod.car.home.app;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;


import com.anod.car.home.BuildConfig;
import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.utils.AppLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author alex
 * @date 2014-09-01
 */
public abstract class MusicAppsActivity extends AppsListActivity {
    private static HashSet<String> sExcludePackages;

    static {
        sExcludePackages = new HashSet<String>(2);
        sExcludePackages.add("com.amazon.kindle");
        sExcludePackages.add("com.google.android.apps.magazines");
        sExcludePackages.add("flipboard.app");
        // Samsung crap
        sExcludePackages.add("com.sec.android.app.storycam");
        sExcludePackages.add("com.sec.android.app.mediasync");
        sExcludePackages.add("com.sec.android.mmapp");
        sExcludePackages.add("com.sec.android.automotive.drivelink");
        sExcludePackages.add("com.sec.android.app.mv.player");
        sExcludePackages.add("com.sec.android.app.voicenote");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getRowLayoutId() {
        return R.layout.all_apps_row;
    }

    @Override
    protected AppsList getAppList(Context context) {
        return new AppsList(CarWidgetApplication.get(context));
    }


    @Override
    public Loader<ArrayList<AppsList.Entry>> onCreateLoader(int id, Bundle args) {
        return new MediaAppsLoader(this, mAppsList);
    }

    static class MediaAppsLoader extends AsyncTaskLoader<ArrayList<AppsList.Entry>> {

        private final AppsList mAppsList;

        public MediaAppsLoader(Context context, AppsList list) {
            super(context);
            mAppsList = list;
        }

        @Override
        public ArrayList<AppsList.Entry> loadInBackground() {
            final PackageManager packageManager = getContext().getPackageManager();
            List<ResolveInfo> apps = packageManager.queryBroadcastReceivers(new Intent(Intent.ACTION_MEDIA_BUTTON), 96);

            // filter duplicate receivers
            HashMap<String, Boolean> receivers = new HashMap<String, Boolean>();

            for (ResolveInfo appInfo : apps) {
                String pkg = appInfo.activityInfo.packageName;
                // App title
                if (sExcludePackages.contains(pkg) || receivers.containsKey(pkg)) {
                    continue;
                }
                String title = appInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString();
                if (BuildConfig.DEBUG) {
                    AppLog.d(appInfo.activityInfo.packageName + "/" + appInfo.activityInfo.applicationInfo.className);
                }
                receivers.put(pkg, true);
                mAppsList.put(appInfo, title);
            }
            mAppsList.sort();
            return mAppsList.getEntries();
        }
    }
}
