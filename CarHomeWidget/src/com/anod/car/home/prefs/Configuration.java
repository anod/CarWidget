package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.model.AllAppsListCache;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.views.LauncherItemPreference;

public class Configuration extends ConfigurationActivity {
	private static final int REQUEST_PICK_SHORTCUT = 2;
	private static final int REQUEST_PICK_APPLICATION = 3;
	private static final int REQUEST_CREATE_SHORTCUT = 4;
	private static final int REQUEST_EDIT_SHORTCUT = 5;
	private static final int REQUEST_BACKUP = 6;

	public static final String EXTRA_CELL_ID = "CarHomeWidgetCellId";
	public static final int INVALID_CELL_ID = -1;

	private ShortcutModel mModel;

	private static final int DIALOG_WAIT = 1;
	private static final int DIALOG_INIT = 3;

	private int mCurrentCellId = INVALID_CELL_ID;

	private static final String LOOK_AND_FEEL = "look-and-feel";
	private static final String INCAR = "incar";
	private static final String BACKUP = "backup";

	private static final String VERSION = "version";
	private static final String ISSUE_TRACKER = "issue-tracker";
	private static final String OTHER = "other";

	public static final boolean BUILD_AMAZON = false;

	private static final String DETAIL_MARKET_URL = "market://details?id=%s";
	private static final String DETAIL_AMAZON_URL = "http://www.amazon.com/gp/mas/dl/android?p=%s";
	private static final String OTHER_MARKET_URL = "market://search?q=pub:\"Alex Gavrishev\"";
	private static final String OTHER_AMAZON_URL = "http://www.amazon.com/gp/mas/dl/android?p=com.anod.car.home.free&showAll=1";

	@Override
	protected int getTitleResource() {
		return R.string.settings;
	}

	@Override
	protected int getXmlResource() {
		return R.xml.preferences;
	}
	
	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		mModel = new ShortcutModel(mContext, mAppWidgetId);
		mModel.init();
		Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);

		initActivityChooser(prefs);

		initLookAndFeel();
		initIncar();
		initOther();
		initBackup();

		int cellId = getIntent().getExtras().getInt(EXTRA_CELL_ID, INVALID_CELL_ID);
		if (cellId != INVALID_CELL_ID) {
			pickShortcut(cellId);
		}

	}

	private void initLookAndFeel() {
		Preference pref = (Preference) findPreference(LOOK_AND_FEEL);
		Intent intent = new Intent(this, ConfigurationLook.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		pref.setIntent(intent);
	}

	private void initIncar() {
		Preference pref = (Preference) findPreference(INCAR);
		Intent intent = new Intent(this, ConfigurationInCar.class);
		pref.setIntent(intent);
	}

	private void initBackup() {
		Preference pref = (Preference) findPreference(BACKUP);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Configuration.this, ConfigurationBackup.class);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				startActivityForResult(intent, REQUEST_BACKUP);
				return true;
			}
		});
	}

	private void initActivityChooser(Main prefs) {
		for (int i = 0; i < PreferencesStorage.LAUNCH_COMPONENT_NUMBER; i++) {
			initLauncherPreference(i);
		}
	}

	private void refreshPreference(LauncherItemPreference pref) {
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

	private void initLauncherPreference(int launchComponentId) {
		String key = PreferencesStorage.getLaunchComponentKey(launchComponentId);
		LauncherItemPreference p = (LauncherItemPreference) findPreference(key);
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
				mModel.dropShortcut(pref.getCellId(), mAppWidgetId);
				refreshPreference(pref);
				return true;
			}

		});
		refreshPreference(p);
	}

	private void initOther() {
		Preference version = (Preference) findPreference(VERSION);
		String versionText = getResources().getString(R.string.version_title);
		String appName = "";
		String versionName = "";
		try {
			PackageManager pm = getPackageManager();
			appName = getApplicationInfo().loadLabel(pm).toString();
			versionName = pm.getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}
		version.setTitle(String.format(versionText, appName, versionName));
		version.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url = (BUILD_AMAZON) ? DETAIL_AMAZON_URL : DETAIL_MARKET_URL;
				Uri uri = Uri.parse(String.format(url, getPackageName()));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		Preference other = (Preference) findPreference(OTHER);
		other.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url = (BUILD_AMAZON) ? OTHER_AMAZON_URL : OTHER_MARKET_URL;
				Uri uri = Uri.parse(url);

				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		Preference issue = (Preference) findPreference(ISSUE_TRACKER);
		issue.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("http://www.facebook.com/pages/Car-Widget-for-Android/220355141336206");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

	}

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_INIT:
			ProgressDialog initDialog = new ProgressDialog(this);
			initDialog.setCancelable(true);
			initDialog.setMessage(getResources().getString(R.string.load_paired_device));
			initDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					finish();
				}
			});
			return initDialog;
		case DIALOG_WAIT:
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setCancelable(true);
			String message = getResources().getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		}
		return null;
	}

	@Override
	public void onBackPressed() {
		if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(getIntent().getAction()) && mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			int[] appWidgetIds = new int[1];
			appWidgetIds[0] = mAppWidgetId;
			Provider appWidgetProvider = Provider.getInstance();
			appWidgetProvider.performUpdate(this, appWidgetIds);
		}
		AllAppsListCache allAppsList = ((CarWidgetApplication) this.getApplicationContext()).getAllAppCache();
		allAppsList.flush();
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
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
			case REQUEST_BACKUP:
				refreshShortcuts(data);
				break;
			}
		} else {
			try {
				dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshShortcuts(Intent data) {
		mModel.init();
		for (int i = 0; i < PreferencesStorage.LAUNCH_COMPONENT_NUMBER; i++) {
			String key = PreferencesStorage.getLaunchComponentName(i, mAppWidgetId);
			LauncherItemPreference p = (LauncherItemPreference) findPreference(key);
			refreshPreference(p);
		}
	}

	private void pickShortcut(int cellId) {
		showDialog(DIALOG_WAIT);
		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();

		shortcutNames.add(getResources().getString(R.string.applications));
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
		shortcutIcons.add(ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_application));
		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

		Intent dataIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		dataIntent.putExtra(EXTRA_CELL_ID, cellId);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, dataIntent);
		pickIntent.putExtra(Intent.EXTRA_TITLE, "Select shortcut");
		pickIntent.putExtras(bundle);

		startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
	}

	private void processShortcut(Intent intent) {
		// Handle case where user selected "Applications"
		String applicationName = getResources().getString(R.string.applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		mCurrentCellId = INVALID_CELL_ID;
		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(Configuration.this, AllAppsActivity.class);
			mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
			startActivityForResultSafely(mainIntent, REQUEST_PICK_APPLICATION);
		} else {
			mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
			startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
		}
	}

	private void completeAddShortcut(Intent data, boolean isApplicationShortcut) {
		if (mCurrentCellId == INVALID_CELL_ID) {
			return;
		}

		final ShortcutInfo info = mModel.saveShortcutIntent(mCurrentCellId, data, isApplicationShortcut);

		if (info != null && info.id != ShortcutInfo.NO_ID) {

			String key = PreferencesStorage.getLaunchComponentName(mCurrentCellId, mAppWidgetId);
			LauncherItemPreference p = (LauncherItemPreference) findPreference(key);
			refreshPreference(p);
		}
		mCurrentCellId = INVALID_CELL_ID;
		try {
			dismissDialog(DIALOG_WAIT);
		} catch (IllegalArgumentException e) {

		}
	}

	private void startEditActivity(int cellId, long shortcutId) {
		Intent editIntent = new Intent(this, ShortcutEditActivity.class);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, shortcutId);
		editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, cellId);
		startActivityForResultSafely(editIntent, REQUEST_EDIT_SHORTCUT);
	}

	private void completeEditShortcut(Intent data) {
		int cellId = data.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, INVALID_CELL_ID);
		long shortcutId = data.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
		if (cellId != INVALID_CELL_ID) {
			String key = PreferencesStorage.getLaunchComponentName(cellId, mAppWidgetId);
			LauncherItemPreference p = (LauncherItemPreference) findPreference(key);
			mModel.reloadShortcut(cellId, shortcutId);
			refreshPreference(p);
		}
	}

	private void startActivityForResultSafely(Intent intent, int requestCode) {
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
			Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity.", e);
		}
	}

}
