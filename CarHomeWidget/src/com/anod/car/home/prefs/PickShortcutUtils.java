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
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.views.ShortcutPreference;
import com.anod.car.home.utils.IntentUtils;

public class PickShortcutUtils {
	private int mCurrentCellId = INVALID_CELL_ID;

	private static final int REQUEST_PICK_SHORTCUT = 2;
	private static final int REQUEST_PICK_APPLICATION = 3;
	private static final int REQUEST_CREATE_SHORTCUT = 4;
	private static final int REQUEST_EDIT_SHORTCUT = 5;
	
	public static final String EXTRA_CELL_ID = "CarHomeWidgetCellId";
	public static final int INVALID_CELL_ID = -1;

	private final ConfigurationActivity mActivity;
	private final ShortcutsModel mModel;
	private final PreferenceKey mPreferenceKey;
	
	interface PreferenceKey {
		String getInitialKey(int position);
		String getCompiledKey(int position);
	}
	
	public PickShortcutUtils(ConfigurationActivity activity, ShortcutsModel model, PreferenceKey key) {
		mActivity = activity;
		mModel = model;
		mPreferenceKey = key;
	}
	
	public ShortcutPreference initLauncherPreference(int position) {
		ShortcutPreference p = (ShortcutPreference) mActivity.findPreference(mPreferenceKey.getInitialKey(position));
		p.setKey(mPreferenceKey.getCompiledKey(position));
		p.setShortcutPosition(position);
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ShortcutPreference pref = (ShortcutPreference) preference;
				int position = pref.getShortcutPosition();
				ShortcutInfo info = mModel.getShortcut(position);
				if (info == null) {
					showActivityPicker(position);
				} else {
					startEditActivity(position, info.id);
				}
				return true;
			}
		});
		p.setOnDeleteClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ShortcutPreference pref = (ShortcutPreference) preference;
				mModel.dropShortcut(pref.getShortcutPosition());
				refreshPreference(pref);
				return true;
			}

		});
		refreshPreference(p);
        return p;
	}
	
	public void showActivityPicker(int position) {
		mActivity.showWaitDialog();
		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();

		shortcutNames.add(mActivity.getString(R.string.applications));
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
		shortcutIcons.add(ShortcutIconResource.fromContext(mActivity, R.drawable.ic_launcher_application));
		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

		Intent dataIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		dataIntent.putExtra(EXTRA_CELL_ID, position);

		Intent pickIntent = new Intent(mActivity, ActivityPicker.class);
		pickIntent.putExtras(bundle);
		pickIntent.putExtra(Intent.EXTRA_INTENT, dataIntent);
		pickIntent.putExtra(Intent.EXTRA_TITLE, mActivity.getString(R.string.select_shortcut_title));

		mActivity.startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
	}
	

	private void startEditActivity(int cellId, long shortcutId) {
		Intent editIntent = IntentUtils.createShortcutEditIntent(mActivity, cellId, shortcutId);
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
				pickShortcut(data);
				mActivity.dismissWaitDialog();
				break;
			default:
			}
		} else {
			mActivity.dismissWaitDialog();
		}
	}
	
	public void refreshPreference(ShortcutPreference pref) {
		int cellId = pref.getShortcutPosition();
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
		if (mCurrentCellId == INVALID_CELL_ID || data == null) {
			return;
		}

		final ShortcutInfo info = mModel.saveShortcutIntent(mCurrentCellId, data, isApplicationShortcut);

		if (info != null && info.id != ShortcutInfo.NO_ID) {
			String key = mPreferenceKey.getCompiledKey(mCurrentCellId);
			ShortcutPreference p = (ShortcutPreference) mActivity.findPreference(key);
			refreshPreference(p);
		}
		mCurrentCellId = INVALID_CELL_ID;
	}

	private void pickShortcut(Intent intent) {
		// Handle case where user selected "Applications"
		String applicationName = mActivity.getString(R.string.applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(mActivity, AllAppsActivity.class);
			startActivityForResultSafely(mainIntent, REQUEST_PICK_APPLICATION);
		} else {
			startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
		}
	}


	
	private void completeEditShortcut(Intent data) {
		int cellId = data.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, INVALID_CELL_ID);
		long shortcutId = data.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
		if (cellId != INVALID_CELL_ID) {
			String key = mPreferenceKey.getCompiledKey(cellId);
			ShortcutPreference p = (ShortcutPreference) mActivity.findPreference(key);
			mModel.reloadShortcut(cellId, shortcutId);
			refreshPreference(p);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("currentCellId", mCurrentCellId);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCurrentCellId = savedInstanceState.getInt("currentCellId", INVALID_CELL_ID);
		}
		
	}
}
