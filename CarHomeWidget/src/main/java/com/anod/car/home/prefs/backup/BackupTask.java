package com.anod.car.home.prefs.backup;

import android.os.AsyncTask;

/**
 * @author alex
 * @date 12/30/13
 */
public class BackupTask extends AsyncTask<String, Void, Integer> {

    private int mType;

    private PreferencesBackupManager mBackupManager;

    private int mAppWidgetId;

    private BackupTaskListner mListener;

    public interface BackupTaskListner {

        void onBackupPreExecute(int type);

        void onBackupFinish(int type, int code);
    }

    public BackupTask(int type, PreferencesBackupManager backupManager, int appWidgetId,
            BackupTaskListner listener) {
        mType = type;
        mBackupManager = backupManager;
        mAppWidgetId = appWidgetId;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.onBackupPreExecute(mType);

    }

    protected Integer doInBackground(String... filenames) {
        String filename = filenames[0];
        if (filename == null) {
            return mBackupManager.doBackupInCar();
        }
        return mBackupManager.doBackupWidget(filename, mAppWidgetId);
    }

    protected void onPostExecute(Integer result) {
        mListener.onBackupFinish(mType, result);
    }
}
