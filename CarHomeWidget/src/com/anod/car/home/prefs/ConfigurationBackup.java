package com.anod.car.home.prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

public class ConfigurationBackup extends ConfigurationActivity {
	private static final int REQUEST_RESTORE_MAIN = 1;
	private static final String RESTORE_BTN_INCAR = "restore-btn-incar";
	private static final String BACKUP_BTN_INCAR = "backup-btn-incar";
    private static final String RESTORE_BTN_MAIN = "restore-btn-main";
	private static final String BACKUP_BTN_MAIN = "backup-btn-main";
	private static final String INCAR_CATEGORY = "backup-incar-category";
	
	private static final int DIALOG_BACKUP_NAME=2;
	
	private static final int TYPE_MAIN = 1;
	private static final int TYPE_INCAR = 2;
	
	private Preference mBackupMainPref;
	private Preference mBackupIncarPref;
	private PreferencesBackupManager mBackupManager;
	private String mLastBackupStr;

	private static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE
		| DateUtils.FORMAT_SHOW_WEEKDAY
		| DateUtils.FORMAT_SHOW_TIME
		| DateUtils.FORMAT_SHOW_YEAR
	;

	@Override
	protected int getXmlResource() {
		return R.xml.preference_backup;
	}

	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {

		mLastBackupStr = getString(R.string.last_backup);
		mBackupMainPref = (Preference) findPreference(BACKUP_BTN_MAIN);
		mBackupIncarPref = (Preference) findPreference(BACKUP_BTN_INCAR);
		mBackupManager = new PreferencesBackupManager(mContext);

		mContext = (Context) this;

		initInCar();
		initBackup();
		updateMainTime();
		updateInCarTime();

	}

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_BACKUP_NAME:
			String defaultFilename = "backup-" + mAppWidgetId;
			// This example shows how to add a custom layout to an AlertDialog
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(R.layout.backup_dialog_enter_name, null);
			final EditText backupName = (EditText) textEntryView.findViewById(R.id.backup_name);
			backupName.setText(defaultFilename);
			return new AlertDialog.Builder(this).setTitle(R.string.backup_current_widget).setView(textEntryView).setPositiveButton(R.string.backup_save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String filename = backupName.getText().toString();
					if (!filename.equals("")) {
						new BackupTask().execute(filename);
					}
				}
			}).setNegativeButton(R.string.backup_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			}).create();
		}
		return super.onCreateDialog(id);
	}

	private void initBackup() {
		mBackupMainPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showDialog(DIALOG_BACKUP_NAME);
				return false;
			}
		});

		Preference restore_main = (Preference) findPreference(RESTORE_BTN_MAIN);
		restore_main.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intentMain = new Intent(mContext, ConfigurationRestore.class);
				intentMain.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				intentMain.putExtra(ConfigurationRestore.EXTRA_TYPE, ConfigurationRestore.TYPE_MAIN);
				startActivityForResult(intentMain, REQUEST_RESTORE_MAIN);
				return true;
			}

		});

	}

	private void initInCar() {
		mBackupIncarPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String arg = null;
				new BackupTask().execute(arg);
				return false;
			}
		});

		Preference restore_incar = (Preference) findPreference(RESTORE_BTN_INCAR);
		Intent intentInCar = new Intent(this, ConfigurationRestore.class);
		intentInCar.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		intentInCar.putExtra(ConfigurationRestore.EXTRA_TYPE, ConfigurationRestore.TYPE_INCAR);
		restore_incar.setIntent(intentInCar);
	}

	private void updateInCarTime() {
		String summary;
		long timeIncar = mBackupManager.getIncarTime();
		if (timeIncar > 0) {
			summary = DateUtils.formatDateTime(this, timeIncar, DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mBackupIncarPref.setSummary(String.format(mLastBackupStr, summary));
	}

	private void updateMainTime() {
		String summary;
		long timeMain = mBackupManager.getMainTime();
		if (timeMain > 0) {
			summary = DateUtils.formatDateTime(this, timeMain, DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mBackupMainPref.setSummary(String.format(mLastBackupStr, summary));
	}

	private void onBackupFinish(int type, int code) {
		Resources r = getResources();
		if (code == PreferencesBackupManager.RESULT_DONE) {
			switch (type) {
			case TYPE_MAIN:
				updateMainTime();
				break;
			case TYPE_INCAR:
				updateInCarTime();
				break;
			}
			Toast.makeText(mContext, r.getString(R.string.backup_done), Toast.LENGTH_SHORT).show();
			return;
		}
		switch (code) {
		case PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE:
			Toast.makeText(mContext, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
			break;
		case PreferencesBackupManager.ERROR_FILE_WRITE:
			Toast.makeText(mContext, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private class BackupTask extends AsyncTask<String, Void, Integer> {
		private int mTaskType;

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WAIT);
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			if (filename == null) {
				mTaskType = TYPE_INCAR;
				return mBackupManager.doBackupInCar();
			}
			mTaskType = TYPE_MAIN;
			return mBackupManager.doBackupMain(filename, mAppWidgetId);
		}

		protected void onPostExecute(Integer result) {
			try {
				dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {
			}
			onBackupFinish(mTaskType, result);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_RESTORE_MAIN:
				updateMainTime();
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
