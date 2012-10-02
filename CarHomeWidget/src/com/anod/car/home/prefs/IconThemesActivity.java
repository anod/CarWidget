package com.anod.car.home.prefs;

import android.content.Intent;

import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheActivity;
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
	
}
