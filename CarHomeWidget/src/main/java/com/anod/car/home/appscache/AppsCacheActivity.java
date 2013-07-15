package com.anod.car.home.appscache;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
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
		setContentView(R.layout.apps_cache_list);
		getListView().setOnItemClickListener(this); 
		int footerViewId = getFooterViewId();
		if (footerViewId > 0) {
			FrameLayout panel = (FrameLayout) findViewById(R.id.panel);
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View footerView = inflater.inflate(R.layout.icon_theme_buttons, null);
			panel.addView(footerView);
			panel.setVisibility(View.VISIBLE);
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

		if (appsCacheEntries == null || appsCacheEntries.isEmpty() || isRefreshCache()) {
			new AppsCacheAsyncTask(this, mAppsList, this).execute(0);
		} else {
			showList(appsCacheEntries);
		}
	}
	
	protected boolean isRefreshCache() {
		return false;
	}
	
	protected void onResumeImpl() {
		// Nothing by default
	}
	
	protected void onCreateImpl(Bundle savedInstanceState) {
		// Nothing by default
	}
	
	private void showList(ArrayList<CacheEntry> cacheEntries) {
		AppsCacheAdapter adapter;
		mItems = getHeadEntries();
		if (mItems == null) {
			mItems = cacheEntries;
		} else {
			mItems.addAll(cacheEntries);
		}
		adapter = new AppsCacheAdapter(this, getRowLayoutId(), mItems, mAppsList);
		// Bind to our new adapter.
		setListAdapter(adapter);
		onItemsSet(mItems);
	}

	protected void onItemsSet(ArrayList<CacheEntry> items) {
		// Nothing by default
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

	protected int getFooterViewId() {
		return 0;
	}

}