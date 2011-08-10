package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.anod.car.home.R;

public class ConfigurationBackup extends PreferenceActivity {
	private static final String RESTORE_BTN_INCAR = "restore-btn-incar";
	private static final String BACKUP_BTN_INCAR = "backup-btn-incar";
    private static final String RESTORE_BTN_MAIN = "restore-btn-main";
	private static final String BACKUP_BTN_MAIN = "backup-btn-main";
	
	private static final int TYPE_MAIN = 1;
	private static final int TYPE_INCAR = 2;
	
    private int mAppWidgetId;   
	private Context mContext;
	private Preference mBackupMainPref;
	private Preference mBackupIncarPref;
	private BackupManager mBackupManager;
	private String mLastBackupStr;

	private static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE
		| DateUtils.FORMAT_SHOW_WEEKDAY
		| DateUtils.FORMAT_SHOW_TIME
		| DateUtils.FORMAT_SHOW_YEAR
	;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	addPreferencesFromResource(R.xml.preference_backup);
    	
    	Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
       
            Intent defaultResultValue = new Intent();
            defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, defaultResultValue);
        } else {
            finish();
        }
        mContext = (Context)this;
        
		mLastBackupStr = getString(R.string.last_backup);        
    	mBackupMainPref = (Preference)findPreference(BACKUP_BTN_MAIN);
    	mBackupIncarPref = (Preference)findPreference(BACKUP_BTN_INCAR);
    	
		mBackupManager = new BackupManager(mContext);
		
       	initBackup();
		updateMainTime();
		updateInCarTime();       	
       	
    }
	
    private void initBackup() {
    	final String filename = "backup-"+mAppWidgetId;
		mBackupMainPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new BackupTask().execute(filename);
				return false;
			}
    	});
		
		mBackupIncarPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String arg = null;
				new BackupTask().execute(arg);
				return false;
			}
    	});
		
    	Preference restore_main = (Preference)findPreference(RESTORE_BTN_MAIN);
    	restore_main.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new RestoreTask().execute(filename);
				return false;
			}
    	});
		
    	Preference restore_incar = (Preference)findPreference(RESTORE_BTN_INCAR);
    	restore_incar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String arg = null;
				new RestoreTask().execute(arg);
				return false;
			}
    	});
    }

	private void updateInCarTime() {
		String summary;
		long timeIncar = mBackupManager.getIncarTime();
		if (timeIncar > 0) {
			summary = DateUtils.formatDateTime(this, timeIncar, DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mBackupIncarPref.setSummary(String.format(mLastBackupStr,summary));
	}

	private void updateMainTime() {
		String summary;
		long timeMain = mBackupManager.getMainTime();
		if (timeMain > 0) {
			summary = DateUtils.formatDateTime(this, timeMain, DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mBackupMainPref.setSummary(String.format(mLastBackupStr,summary));
	}

	private void onBackupFinish(int type, int code) {
		if (code == BackupManager.RESULT_DONE) {
			switch (type) {
				case TYPE_MAIN:
					updateMainTime();
				break;
				case TYPE_INCAR:
					updateInCarTime();
				break;
			}
			Toast.makeText(mContext, "Backup is done.", Toast.LENGTH_SHORT).show();
			return;
		}
		switch (code) {
			case BackupManager.ERROR_STORAGE_NOT_AVAILABLE:
				Toast.makeText(mContext, "External storage is not avialable", Toast.LENGTH_SHORT).show();
			break;
			case BackupManager.ERROR_FILE_WRITE:
            	Toast.makeText(mContext, "BackupManager failed to write the file", Toast.LENGTH_SHORT).show();
            break;
		}
	}

	private void onRestoreFinish(int type, int code) {
		if (code == BackupManager.RESULT_DONE) {
			Toast.makeText(mContext, "Restore is done.", Toast.LENGTH_SHORT).show();
			return;
		}
		switch (code) {
			case BackupManager.ERROR_STORAGE_NOT_AVAILABLE:
				Toast.makeText(mContext, "External storage is not avialable", Toast.LENGTH_SHORT).show();
			break;
			case BackupManager.ERROR_DESERIALIZE:
				Toast.makeText(mContext, "Failed to deserialize backup", Toast.LENGTH_SHORT).show();		
			break;
			case BackupManager.ERROR_FILE_READ:
            	Toast.makeText(mContext, "BackupManager failed to read the file", Toast.LENGTH_SHORT).show();
            break;
			case BackupManager.ERROR_FILE_NOT_EXIST:
	            Toast.makeText(mContext, "Backup file is not exists", Toast.LENGTH_SHORT).show();
	        break;
		}
	}

	private class BackupTask extends AsyncTask<String, Void, Integer> {
		 private int mTaskType;
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
	    	 onBackupFinish(mTaskType, result);
	     }
	}
	
	private class RestoreTask extends AsyncTask<String, Void, Integer> {
		 private int mTaskType;
	     protected Integer doInBackground(String... filenames) {
	    	 String filename = filenames[0];
	    	 if (filename == null) {
	    		 mTaskType = TYPE_INCAR;
	    		 return mBackupManager.doRestoreInCar();
	    	 }
	    	 mTaskType = TYPE_MAIN;
	    	 return mBackupManager.doRestoreMain(filename, mAppWidgetId);
	     }

	     protected void onPostExecute(Integer result) {
	    	 onRestoreFinish(mTaskType, result);
	     }
	}	
}
