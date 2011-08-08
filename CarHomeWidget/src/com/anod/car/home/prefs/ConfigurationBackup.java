package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.format.DateUtils;

import com.anod.car.home.R;

public class ConfigurationBackup extends PreferenceActivity {
	private static final String RESTORE_BTN_INCAR = "restore-btn-incar";
	private static final String BACKUP_BTN_INCAR = "backup-btn-incar";
    private static final String RESTORE_BTN_MAIN = "restore-btn-main";
	private static final String BACKUP_BTN_MAIN = "backup-btn-main";
	
    private int mAppWidgetId;
	private Context mContext;

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
        
       	initBackup();
       	
    }
	
	   private void initBackup() {
	    	String summary;
	    	int format = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_WEEKDAY
	    		| DateUtils.FORMAT_SHOW_TIME
	    		| DateUtils.FORMAT_SHOW_YEAR
	    	;
	    	String lastBackupStr = getString(R.string.last_backup);
	    	
	    	final String filename = "backup-"+mAppWidgetId+".json";
	    	Preference backup_main = (Preference)findPreference(BACKUP_BTN_MAIN);
			BackupManager bm = new BackupManager(mContext);
			long timeMain = bm.getMainTime();
			if (timeMain > 0) {
				summary = DateUtils.formatDateTime(this, timeMain, format);
			} else {
				summary = getString(R.string.never);
			}
			backup_main.setSummary(String.format(lastBackupStr,summary));
	    	backup_main.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					BackupManager b = new BackupManager(mContext);
					b.doBackupMain(filename, mAppWidgetId);			
					return false;
				}
	    	});
	    	Preference restore_main = (Preference)findPreference(RESTORE_BTN_MAIN);
	    	restore_main.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					BackupManager b = new BackupManager(mContext);
					b.doRestoreMain(filename, mAppWidgetId);
					return false;
				}
	    	});
	    	
	    	Preference backup_incar = (Preference)findPreference(BACKUP_BTN_INCAR);
			long timeIncar = bm.getIncarTime();
			if (timeIncar > 0) {
				summary = DateUtils.formatDateTime(this, timeIncar, format);
			} else {
				summary = getString(R.string.never);
			}
			backup_incar.setSummary(String.format(lastBackupStr,summary));
	    	backup_incar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					BackupManager b = new BackupManager(mContext);
					b.doBackupInCar();
					return false;
				}
	    	});
	    	Preference restore_incar = (Preference)findPreference(RESTORE_BTN_INCAR);
	    	restore_incar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					BackupManager b = new BackupManager(mContext);
					b.doRestoreInCar();
					return false;
				}
	    	});
	    }
	 
}
