package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheActivity;
import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.utils.IconPackUtils;
import com.anod.car.home.utils.Utils;

public class IconThemesActivity extends AppsCacheActivity implements OnClickListener {
	private int mCurrentSelected = 0;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private Main mPrefs;
	private String mThemePackageName = null;
	private boolean mRefresh;
	private static final String ADW_ICON_THEME_MARKET_URL = "market://search?q=ADW Icon Theme";

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mAppWidgetId = Utils.readAppWidgetId(savedInstanceState,getIntent());
		mRefresh = false;
	}

	@Override
	protected void onResumeImpl() {
		mPrefs = PreferencesStorage.loadMain(this, mAppWidgetId);
		mThemePackageName = mPrefs.getIconsTheme();
	}

	@Override
	protected boolean isRefreshCache() {
		return mRefresh;
	}

	@Override
	public void onItemsSet(ArrayList<CacheEntry> items) {
		if (mThemePackageName != null) {
			for(int i = 1; i < items.size(); i++) {
				CacheEntry entry = items.get(i);
				if (entry.componentName != null && entry.componentName.getPackageName().equals(mThemePackageName)) {
					mCurrentSelected = i;
					break;
				}
			}
		}
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
		mThemePackageName  = (entry.componentName == null) ? null: entry.componentName.getPackageName(); 
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
		View v = getLayoutInflater().inflate(R.layout.icon_theme_buttons, null);
		Button okButton = (Button)v.findViewById(R.id.btn_ok);
		okButton.setOnClickListener(this);
		Button downloadButton = (Button)v.findViewById(R.id.btn_download);
		downloadButton.setOnClickListener(this);
		Button cancelButton = (Button)v.findViewById(R.id.btn_cancel);
		cancelButton.setOnClickListener(this);

		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Utils.saveAppWidgetId(outState, mAppWidgetId);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_ok) {
			String prevTheme = mPrefs.getIconsTheme();
			boolean update = false;
			if (mThemePackageName == null && prevTheme != null) {
				update = true;
			} else if(mThemePackageName != null && prevTheme == null) {
				update = true;
			} else if (!mThemePackageName.equals(prevTheme)) {
				update = true;
			}
			if (update) {
				mPrefs.setIconsTheme(mThemePackageName);
				PreferencesStorage.saveMain(this, mPrefs, mAppWidgetId);
			}
			finish();
			return;
		}
		if (v.getId() == R.id.btn_download) {
			Uri uri = Uri.parse(ADW_ICON_THEME_MARKET_URL);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			mRefresh = true;
			startActivity(intent);
			return;
		}
		if (v.getId() == R.id.btn_cancel) {
			finish();
			return;
		}
		
	}
}
