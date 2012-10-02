package com.anod.car.home.appscache;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.appscache.AppsCacheAsyncTask.Callback;
import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;

public abstract class AppsCacheActivity extends ListActivity implements OnItemClickListener, Callback {
	private AppsListCache mAppsList;

	abstract protected boolean isShowTitle();
	abstract protected int getRowLayoutId();
	abstract protected void onEntryClick(CacheEntry entry);
	abstract protected AppsListCache getAppListCache(CarWidgetApplication app);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!isShowTitle()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		super.onCreate(savedInstanceState);
		getListView().setOnItemClickListener(this);
		setVisible(false);

		mAppsList = getAppListCache((CarWidgetApplication) this.getApplicationContext());
		ArrayList<CacheEntry> appsCacheEntries = (mAppsList == null) ? null : mAppsList.getCacheEntries();

		if (appsCacheEntries == null || appsCacheEntries.size() == 0) {
			new AppsCacheAsyncTask(this, mAppsList, this).execute(0);
		} else {
			showList(appsCacheEntries);
		}
	}

	private void showList(ArrayList<CacheEntry> cacheEntries) {
		AppsCacheAdapter adapter;
		ArrayList<CacheEntry> headItems = getHeadEntries();
		if (headItems != null) {
			headItems.addAll(cacheEntries);
			adapter = new AppsCacheAdapter(this, getRowLayoutId(), headItems, mAppsList);
		} else {
			adapter = new AppsCacheAdapter(this, getRowLayoutId(), cacheEntries, mAppsList);
		}
		// Bind to our new adapter.
		setListAdapter(adapter);
		try {
			setVisible(true);
		} catch (Exception e) {
			Log.d("CarHomeWidget", "Cannot set list visible");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CacheEntry entry = mAppsList.getCacheEntries().get(position);
		onEntryClick(entry);
	}

	@Override
	public void onResult(ArrayList<CacheEntry> cacheEntries) {
		showList(cacheEntries);
	}

	protected ArrayList<CacheEntry> getHeadEntries() {
		return null;
	}

}