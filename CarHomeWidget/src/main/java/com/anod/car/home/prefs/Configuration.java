package com.anod.car.home.prefs;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.anod.car.home.DebugActivity;
import com.anod.car.home.R;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.PickShortcutUtils.PreferenceKey;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.views.ShortcutPreference;

public class Configuration extends ConfigurationFragment implements PreferenceKey, ShortcutPreference.DropCallback {
	private static final int REQUEST_BACKUP = 6;
	private ShortcutsModel mModel;
	private PickShortcutUtils mPickShortcutUtils;
	private static final String LOOK_AND_FEEL = "look-and-feel";
	private static final String INCAR = "incar";
	private static final String BACKUP = "backup";

	@Override
	protected int getXmlResource() {
		return R.xml.preferences;
	}
	
	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		mModel = new LauncherShortcutsModel(mContext, mAppWidgetId);
		mModel.init();
		mPickShortcutUtils = new PickShortcutUtils(this, mModel, this);
		mPickShortcutUtils.onRestoreInstanceState(savedInstanceState);
		initShortcuts();

		setIntent("debug-activity", DebugActivity.class, 0);
		setIntent(LOOK_AND_FEEL, LookAndFeelActivity.class, mAppWidgetId);
		showFragmentOnClick(BACKUP, ConfigurationBackup.class);
		showFragmentOnClick(INCAR, ConfigurationInCar.class);
		initBackup();

		int cellId = getActivity().getIntent().getExtras().getInt(PickShortcutUtils.EXTRA_CELL_ID, PickShortcutUtils.INVALID_CELL_ID);
		if (cellId != PickShortcutUtils.INVALID_CELL_ID) {
			mPickShortcutUtils.showActivityPicker(cellId);
		}
        
		
	}

	@Override
	protected int getOptionsMenuResource() {

		return R.menu.configuration;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.apply) {
			ConfigurationActivity act = (ConfigurationActivity)getActivity();
			act.beforeFinish();
			act.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPickShortcutUtils.onSaveInstanceState(outState);
	}

	private void initBackup() {
		Preference pref = findPreference(BACKUP);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = ConfigurationActivity.createFragmentIntent(mContext, ConfigurationBackup.class);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				startActivityForResult(intent, REQUEST_BACKUP);
				return true;
			}
		});
	}

	private void initShortcuts() {
		for (int i = 0; i < PreferencesStorage.LAUNCH_COMPONENT_NUMBER; i++) {
			ShortcutPreference p = mPickShortcutUtils.initLauncherPreference(i);
			p.setDropCallback(this);
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_BACKUP) {
			refreshShortcuts();
		} else {
			mPickShortcutUtils.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshShortcuts() {
		mModel.init();
		for (int i = 0; i < PreferencesStorage.LAUNCH_COMPONENT_NUMBER; i++) {
			String key = PreferencesStorage.getLaunchComponentName(i, mAppWidgetId);
			ShortcutPreference p = (ShortcutPreference) findPreference(key);
			mPickShortcutUtils.refreshPreference(p);
		}
	}

	@Override
	public String getInitialKey(int position) {
		return PreferencesStorage.getLaunchComponentKey(position);
	}

	@Override
	public String getCompiledKey(int position) {
		return PreferencesStorage.getLaunchComponentName(position, mAppWidgetId);
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public int onScrollRequest(int top) {

		int lastVisiblePos = getListView().getLastVisiblePosition();
		int firstVisiblePos = getListView().getFirstVisiblePosition();
		int lastVisibleIdx = lastVisiblePos - firstVisiblePos;
		if (lastVisibleIdx > 0) {
			lastVisibleIdx -= 1;
		}
		Log.d("onScrollRequest", "Last visible position: " + lastVisiblePos);
		View child = getListView().getChildAt(lastVisibleIdx);
		if (child == null) {
			Log.d("onScrollRequest", "Child is null ");
			return 0;
		}
		int lastVisibleTop = (int) child.getTop();
		if (top >= lastVisibleTop) {
//			getListView().sc
		}
		Log.d("onScrollRequest", "Last visible Top: " + lastVisibleTop + " Drop Top: " + top);

		return 0;
	}

	@Override
	public boolean onDrop(int oldCellId, int newCellId) {
		if  (oldCellId == newCellId) {
			return false;
		}
		mModel.move(oldCellId,newCellId);
		refreshShortcuts();
		return true;
	}
}
