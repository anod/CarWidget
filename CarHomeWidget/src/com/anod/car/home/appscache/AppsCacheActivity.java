package com.anod.car.home.appscache;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
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
	private ArrayList<CacheEntry> mItems;
	abstract protected boolean isShowTitle();
	abstract protected int getRowLayoutId();
	abstract protected void onEntryClick(int position, CacheEntry entry);
	abstract protected AppsListCache getAppListCache(CarWidgetApplication app);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!isShowTitle()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		super.onCreate(savedInstanceState);
		getListView().setOnItemClickListener(this);
		View footerView = getFooterView();
		if (footerView != null) {
			getListView().addFooterView(footerView);
		}
		onCreateImpl(savedInstanceState);
		setResult(Activity.RESULT_OK);
		
		mAppsList = getAppListCache((CarWidgetApplication) this.getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		onResumeImpl();
		ArrayList<CacheEntry> appsCacheEntries = (mAppsList == null) ? null : mAppsList.getCacheEntries();

		if (appsCacheEntries == null || appsCacheEntries.size() == 0 || isRefreshCache()) {
			new AppsCacheAsyncTask(this, mAppsList, this).execute(0);
		} else {
			showList(appsCacheEntries);
		}
	}
	
	protected boolean isRefreshCache() {
		return false;
	}
	
	protected void onResumeImpl() {
		
	}
	
	protected void onCreateImpl(Bundle savedInstanceState) {
	}
	
	private void showList(ArrayList<CacheEntry> cacheEntries) {
		AppsCacheAdapter adapter;
		mItems = getHeadEntries();
		if (mItems != null) {
			mItems.addAll(cacheEntries);
		} else {
			mItems = cacheEntries;
		}
		adapter = new AppsCacheAdapter(this, getRowLayoutId(), mItems, mAppsList);
		// Bind to our new adapter.
		setListAdapter(adapter);
		onItemsSet(mItems);
	}

	protected void onItemsSet(ArrayList<CacheEntry> items) {
		
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CacheEntry entry = mItems.get(position);
		onEntryClick(position, entry);
	}

	@Override
	public void onResult(ArrayList<CacheEntry> cacheEntries) {
		showList(cacheEntries);
	}

	protected ArrayList<CacheEntry> getHeadEntries() {
		return null;
	}

	protected View getFooterView() {
		return null;
	}

}