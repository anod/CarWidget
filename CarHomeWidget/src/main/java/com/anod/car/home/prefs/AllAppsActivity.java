package com.anod.car.home.prefs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheActivity;
import com.anod.car.home.model.AppsList;

public class AllAppsActivity extends AppsCacheActivity {
	
    private Intent createActivityIntent(ComponentName className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
	}

	@Override
	public void onIntentFilterInit(Intent intent) {
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
	}

	@Override
	protected boolean isShowTitle() {
		return false;
	}

	@Override
	protected int getRowLayoutId() {
        return R.layout.all_apps_row;
	}

	@Override
	protected void onEntryClick(int position, AppsList.Entry entry) {
        Intent intent = createActivityIntent(entry.componentName);
        setResult(RESULT_OK, intent);
        finish();
	}

	@Override
	protected AppsList getAppList(Context context) {
		return CarWidgetApplication.provide(context).getAppListCache();
	}


}
