package com.anod.car.home.prefs;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
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
	private static final String BACKUP_PATH = "backup-path";
	private static final int REQUEST_RESTORE_MAIN = 1;
	private static final String RESTORE_BTN_INCAR = "restore-btn-incar";
	private static final String BACKUP_BTN_INCAR = "backup-btn-incar";
    private static final String RESTORE_BTN_MAIN = "restore-btn-main";
	private static final String BACKUP_BTN_MAIN = "backup-btn-main";
	
	private static final int DIALOG_BACKUP_NAME=2;
	private static final int DIALOG_PRO = 3;

	private static final int TYPE_MAIN = 1;
	private static final int TYPE_INCAR = 2;
	
	private Preference mBackupMainPref;
	private Preference mBackupIncarPref;
	private PreferencesBackupManager mBackupManager;
	private String mLastBackupStr;


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
		Preference backupPathPref = (Preference) findPreference(BACKUP_PATH);
		final File backupFile = mBackupManager.getBackupDir();
		backupPathPref.setSummary(backupFile.getAbsolutePath());
		backupPathPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setAction("pl.solidexplorer.action.BROWSE_TO");
				intent.setData(Uri.fromFile(backupFile));
				try {
					startActivity(intent);
				} catch (Exception e) {
					Utils.logw(e.getMessage());
				}
				return false;
			}
		});
		
		updateMainTime();
		updateInCarTime();

	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_BACKUP_NAME) {
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
					//Nothing
				}
			}).create();
		} else if (id == DIALOG_PRO) {
			return TrialDialogs.buildProOnlyDialog(this);
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

		Preference restoreMain = (Preference) findPreference(RESTORE_BTN_MAIN);
		restoreMain.setOnPreferenceClickListener(new OnPreferenceClickListener() {

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

		Preference restoreIncar = (Preference) findPreference(RESTORE_BTN_INCAR);
		Version version = new Version(this);
		if (version.isFree()) {
			restoreIncar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
				showDialog(DIALOG_PRO);
				return false;
				}
			});
		} else {
			Intent intentInCar = new Intent(this, ConfigurationRestore.class);
			intentInCar.putExtra(ConfigurationRestore.EXTRA_TYPE, ConfigurationRestore.TYPE_INCAR);
			restoreIncar.setIntent(intentInCar);
		}
	}

	private void updateInCarTime() {
		String summary;
		long timeIncar = mBackupManager.getIncarTime();
		if (timeIncar > 0) {
			summary = DateUtils.formatDateTime(this, timeIncar, PreferencesBackupManager.DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mBackupIncarPref.setSummary(String.format(mLastBackupStr, summary));
	}

	private void updateMainTime() {
		String summary;
		long timeMain = mBackupManager.getMainTime();
		if (timeMain > 0) {
			summary = DateUtils.formatDateTime(this, timeMain, PreferencesBackupManager.DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mBackupMainPref.setSummary(String.format(mLastBackupStr, summary));
	}

	private void onBackupFinish(int type, int code) {
		Resources r = getResources();
		if (code == PreferencesBackupManager.RESULT_DONE) {
			if (type == TYPE_MAIN) {
				updateMainTime();
			} else if (type == TYPE_INCAR) {
				updateInCarTime();
			}
			Toast.makeText(mContext, r.getString(R.string.backup_done), Toast.LENGTH_SHORT).show();
			return;
		}
		if (code == PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE) {
			Toast.makeText(mContext, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
		} else if (code == PreferencesBackupManager.ERROR_FILE_WRITE) {
			Toast.makeText(mContext, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
		}
	}

	public class BackupTask extends AsyncTask<String, Void, Integer> {
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
				Utils.logd(e.getMessage());
			}
			onBackupFinish(mTaskType, result);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_RESTORE_MAIN) {
			updateMainTime();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
