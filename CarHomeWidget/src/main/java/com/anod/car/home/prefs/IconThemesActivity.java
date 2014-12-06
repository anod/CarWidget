package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheActivity;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.IconPackUtils;
import com.anod.car.home.utils.Utils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class IconThemesActivity extends AppsCacheActivity {
	private int mCurrentSelected;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private Main mPrefs;
	private String mThemePackageName;
	private boolean mRefresh;
	private static final String ADW_ICON_THEME_MARKET_URL = "market://search?q=ADW Theme";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mAppWidgetId = Utils.readAppWidgetId(savedInstanceState,getIntent());
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			AppLog.d("Invalid AppWidgetId");
			finish();
		}
		mRefresh = false;

        ButterKnife.inject(this);
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
	public void onItemsSet(ArrayList<AppsList.Entry> items) {
		if (mThemePackageName != null) {
			for(int i = 1; i < items.size(); i++) {
				AppsList.Entry entry = items.get(i);
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
		IconPackUtils.fillAdwThemeIntent(intent);
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
	protected void onEntryClick(int position, AppsList.Entry entry) {
		mThemePackageName  = (entry.componentName == null) ? null: entry.componentName.getPackageName(); 
		getListView().setItemChecked(position, true);
	}

	@Override
	protected AppsList getAppList(Context context) {
		return CarWidgetApplication.provide(context).getIconThemesCache();
	}

	@Override
	protected ArrayList<AppsList.Entry> getHeadEntries() {
		ArrayList<AppsList.Entry> head = new ArrayList<AppsList.Entry>(1);
		AppsList.Entry none = new AppsList.Entry();
		none.title = getString(R.string.none);
		head.add(none);
		return head;
	}

	@Override
	protected int getFooterViewId() {
        return R.layout.icon_theme_buttons;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Utils.saveAppWidgetId(outState, mAppWidgetId);
	}

    @OnClick(R.id.btn_ok)
    public void onSave() {
        String prevTheme = mPrefs.getIconsTheme();
        boolean update = false;
        if (mThemePackageName == null && prevTheme != null) {
            update = true;
        } else if(mThemePackageName != null && prevTheme == null) {
            update = true;
        } else if (mThemePackageName != null && prevTheme != null && !mThemePackageName.equals(prevTheme)) {
            update = true;
        }
        if (update) {
            mPrefs.setIconsTheme(mThemePackageName);
            PreferencesStorage.saveMain(this, mPrefs, mAppWidgetId);
        }
        finish();
    }
    @OnClick(R.id.btn_download)
    public void onDownload() {
        Uri uri = Uri.parse(ADW_ICON_THEME_MARKET_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mRefresh = true;
        startActivity(intent);
    }

    @OnClick(R.id.btn_cancel)
	public void onCancel(View v) {
		finish();
	}
}
