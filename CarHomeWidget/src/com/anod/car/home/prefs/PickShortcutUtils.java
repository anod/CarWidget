package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.prefs.views.LauncherItemPreference;

public class PickShortcutUtils {

	private int mCurrentCellId = INVALID_CELL_ID;

	private static final int REQUEST_PICK_SHORTCUT = 2;
	private static final int REQUEST_PICK_APPLICATION = 3;
	private static final int REQUEST_CREATE_SHORTCUT = 4;
	private static final int REQUEST_EDIT_SHORTCUT = 5;
	
	public static final String EXTRA_CELL_ID = "CarHomeWidgetCellId";
	public static final int INVALID_CELL_ID = -1;

	
	private ConfigurationActivity mActivity;
	private int mAppWidgetId;
	private ShortcutModel mModel;
	
	public PickShortcutUtils(ConfigurationActivity activity, int appWidgetId, ShortcutModel model) {
		mActivity = activity;
		mAppWidgetId = appWidgetId;
		mModel = model;
	}
	
	public void initLauncherPreference(int launchComponentId) {
		String key = PreferencesStorage.getLaunchComponentKey(launchComponentId);
		LauncherItemPreference p = (LauncherItemPreference) mActivity.findPreference(key);
		p.setKey(PreferencesStorage.getLaunchComponentName(launchComponentId, mAppWidgetId));
		p.setCellId(launchComponentId);
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				LauncherItemPreference pref = (LauncherItemPreference) preference;
				int cellId = pref.getCellId();
				ShortcutInfo info = mModel.getShortcut(cellId);
				if (info == null) {
					pickShortcut(cellId);
				} else {
					startEditActivity(cellId, info.id);
				}
				return true;
			}
		});
		p.setOnDeleteClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				LauncherItemPreference pref = (LauncherItemPreference) preference;
				mModel.dropShortcut(pref.getCellId());
				refreshPreference(pref);
				return true;
			}

		});
		refreshPreference(p);
	}
	
	public void pickShortcut(int cellId) {
		mActivity.showWaitDialog();
		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();

		shortcutNames.add(mActivity.getResources().getString(R.string.applications));
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
		shortcutIcons.add(ShortcutIconResource.fromContext(mActivity, R.drawable.ic_launcher_application));
		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

		Intent dataIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		dataIntent.putExtra(EXTRA_CELL_ID, cellId);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, dataIntent);
		pickIntent.putExtra(Intent.EXTRA_TITLE, "Select shortcut");
		pickIntent.putExtras(bundle);

		mActivity.startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
	}
	

	private void startEditActivity(int cellId, long shortcutId) {
		Intent editIntent = new Intent(mActivity, ShortcutEditActivity.class);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, shortcutId);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, cellId);
		startActivityForResultSafely(editIntent, REQUEST_EDIT_SHORTCUT);
	}
	
	private void startActivityForResultSafely(Intent intent, int requestCode) {
		try {
			mActivity.startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(mActivity, mActivity.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(mActivity, mActivity.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
			Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity.", e);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICK_APPLICATION:
				completeAddShortcut(data, true);
				break;
			case REQUEST_CREATE_SHORTCUT:
				completeAddShortcut(data, false);
				break;
			case REQUEST_EDIT_SHORTCUT:
				completeEditShortcut(data);
				break;
			case REQUEST_PICK_SHORTCUT:
				processShortcut(data);
				break;
			}
		} else {
			mActivity.dismissWaitDialog();
		}
	}
	
	public void refreshPreference(LauncherItemPreference pref) {
		int cellId = pref.getCellId();
		ShortcutInfo info = mModel.getShortcut(cellId);
		if (info == null) {
			pref.setTitle(R.string.set_shortcut);
			pref.setIconResource(R.drawable.ic_add_shortcut);
			pref.showButtons(false);
		} else {
			pref.setIconBitmap(info.getIcon());
			pref.setTitle(info.title);
			pref.showButtons(true);
		}
	}
	

	private void completeAddShortcut(Intent data, boolean isApplicationShortcut) {
		if (mCurrentCellId == INVALID_CELL_ID) {
			return;
		}

		final ShortcutInfo info = mModel.saveShortcutIntent(mCurrentCellId, data, isApplicationShortcut);

		if (info != null && info.id != ShortcutInfo.NO_ID) {
			String key = PreferencesStorage.getLaunchComponentName(mCurrentCellId, mAppWidgetId);
			LauncherItemPreference p = (LauncherItemPreference) mActivity.findPreference(key);
			refreshPreference(p);
		}
		mCurrentCellId = INVALID_CELL_ID;
		mActivity.dismissWaitDialog();
	}
	


	private void processShortcut(Intent intent) {
		// Handle case where user selected "Applications"
		String applicationName = mActivity.getResources().getString(R.string.applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		mCurrentCellId = INVALID_CELL_ID;
		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(mActivity, AllAppsActivity.class);
			mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
			startActivityForResultSafely(mainIntent, REQUEST_PICK_APPLICATION);
		} else {
			mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
			startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
		}
	}



	private void completeEditShortcut(Intent data) {
		int cellId = data.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, INVALID_CELL_ID);
		long shortcutId = data.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
		if (cellId != INVALID_CELL_ID) {
			String key = PreferencesStorage.getLaunchComponentName(cellId, mAppWidgetId);
			LauncherItemPreference p = (LauncherItemPreference) mActivity.findPreference(key);
			mModel.reloadShortcut(cellId, shortcutId);
			refreshPreference(p);
		}
	}
}
