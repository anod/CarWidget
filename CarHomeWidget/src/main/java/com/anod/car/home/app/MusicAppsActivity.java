package com.anod.car.home.app;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;


import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.app.AppsListActivity;
import com.anod.car.home.model.AppsList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @date 2014-09-01
 */
public abstract class MusicAppsActivity extends AppsListActivity {

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
        return new AppsList(CarWidgetApplication.getApplication(context));
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

            for (ResolveInfo appInfo : apps) {
                // App title
                String title = appInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString();
                mAppsList.put(appInfo, title);
            }
            mAppsList.sort();
            return mAppsList.getEntries();
        }
    }
}
