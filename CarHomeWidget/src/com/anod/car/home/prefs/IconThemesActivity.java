package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheActivity;
import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.utils.IconPackUtils;
import com.anod.car.home.utils.Utils;

public class IconThemesActivity extends AppsCacheActivity {
	private int mCurrentSelected = 0;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private Main mPrefs;

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mAppWidgetId = Utils.readAppWidgetId(savedInstanceState,getIntent());
		mPrefs = PreferencesStorage.loadMain(this, mAppWidgetId);
	}



	@Override
	public void onResult(ArrayList<CacheEntry> cacheEntries) {
		super.onResult(cacheEntries);
		getListView().setItemChecked(mCurrentSelected, true);
	}

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
	protected void onEntryClick(int position, CacheEntry entry) {
		// TODO Auto-generated method stub
		getListView().setItemChecked(position, true);
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

	@Override
	protected View getFooterView() {
		return getLayoutInflater().inflate(R.layout.icon_theme_buttons, null);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Utils.saveAppWidgetId(outState, mAppWidgetId);
	}
}
