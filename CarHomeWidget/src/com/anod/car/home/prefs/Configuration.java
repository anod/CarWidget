package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.model.AllAppsListCache;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.PickShortcutUtils.PreferenceKey;
import com.anod.car.home.prefs.views.ShortcutPreference;

public class Configuration extends ConfigurationActivity implements PreferenceKey {
	private static final int REQUEST_BACKUP = 6;

	private ShortcutsModel mModel;

	private PickShortcutUtils mPickShortcutUtils;

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
	protected int getXmlResource() {
		return R.xml.preferences;
	}
	
	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		mModel = new LauncherShortcutsModel(mContext, mAppWidgetId);
		mModel.init();
		mPickShortcutUtils = new PickShortcutUtils(this, mModel, this);
		mPickShortcutUtils.onRestoreInstanceState(savedInstanceState);
		initActivityChooser();
		
		setIntent(LOOK_AND_FEEL, SkinPreviewActivity.class, mAppWidgetId);
		setIntent(INCAR, ConfigurationInCar.class, 0);
		initOther();
		initBackup();

		int cellId = getIntent().getExtras().getInt(PickShortcutUtils.EXTRA_CELL_ID, PickShortcutUtils.INVALID_CELL_ID);
		if (cellId != PickShortcutUtils.INVALID_CELL_ID) {
			mPickShortcutUtils.pickShortcut(cellId);
		}

	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.apply) {
			requestWidgetUpdate();
			finish();
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

	private void initActivityChooser() {
		for (int i = 0; i < PreferencesStorage.LAUNCH_COMPONENT_NUMBER; i++) {
			mPickShortcutUtils.initLauncherPreference(i);
		}
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
				Uri uri = Uri.parse("https://plus.google.com/118206296686390552505/");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

	}

	@Override
	public void onBackPressed() {
		requestWidgetUpdate();
		AllAppsListCache allAppsList = ((CarWidgetApplication) this.getApplicationContext()).getAllAppCache();
		allAppsList.flush();
		super.onBackPressed();
	}

	private void requestWidgetUpdate() {
		if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(getIntent().getAction()) && mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			int[] appWidgetIds = new int[1];
			appWidgetIds[0] = mAppWidgetId;
			Provider appWidgetProvider = Provider.getInstance();
			appWidgetProvider.performUpdate(this, appWidgetIds);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_BACKUP) {
			refreshShortcuts(data);
		} else {
			mPickShortcutUtils.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshShortcuts(Intent data) {
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




}
