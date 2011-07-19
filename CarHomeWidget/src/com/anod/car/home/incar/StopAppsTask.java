package com.anod.car.home.incar;

import android.app.ActivityManager;
import android.os.AsyncTask;
import android.util.Log;

public class StopAppsTask extends AsyncTask<String, String, Boolean>{
	private ActivityManager mActivityManager;
	
	public void setActivityManager(ActivityManager am) {
		mActivityManager = am;
	}
	
	@Override
    protected Boolean doInBackground(String... packageNames) {
        int count = packageNames.length;
        for (int i = 0; i < count; i++) {
        	try {
        		Log.d("CarHomeWidget", "stop app [" + packageNames[i] + "]");
        		mActivityManager.killBackgroundProcesses(packageNames[i]);
        	} catch (Exception e) {
        		Log.d("CarHomeWidget", "Failed to stop app [" + packageNames[i] + "]");
			}
        }
        return true;
    }
}
