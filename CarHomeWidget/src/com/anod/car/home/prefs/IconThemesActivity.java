package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.content.Intent;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheActivity;
import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;
import com.anod.car.home.utils.IconPackUtils;

public class IconThemesActivity extends AppsCacheActivity {

	@Override
	public void onIntentFilterInit(Intent intent) {
		IconPackUtils.fillAdwIconPackIntent(intent);
		
	}

	@Override
	protected boolean isShowTitle() {
		return true;
	}

	@Override
	protected int getRowLayoutId() {
		return R.layout.icon_theme_row;
	}

	@Override
	protected void onEntryClick(CacheEntry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected AppsListCache getAppListCache(CarWidgetApplication app) {
		app.initIconThemesCache();
		return app.getIconThemesCache();
	}

	@Override
	protected ArrayList<CacheEntry> getHeadEntries() {
		ArrayList<CacheEntry> head = new ArrayList<CacheEntry>(1);
		CacheEntry none = new CacheEntry();
		none.title = getString(R.string.none);
		head.add(none);
		return head;
	}
	
}
