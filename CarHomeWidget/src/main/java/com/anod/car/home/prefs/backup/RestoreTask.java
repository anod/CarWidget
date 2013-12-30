package com.anod.car.home.prefs.backup;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.anod.car.home.R;

import java.io.InputStream;

/**
 * @author alex
 * @date 12/30/13
 */
public class RestoreTask extends AsyncTask<Uri, Void, Integer> {
	private int mType;
	private PreferencesBackupManager mBackupManager;
	private int mAppWidgetId;
	private RestoreTaskListner mListener;

	public interface RestoreTaskListner {
		void onRestorePreExecute(int type);
		void onRestoreFinish(int type,int code);
	}

	public RestoreTask(int type, PreferencesBackupManager backupManager, int appWidgetId, RestoreTaskListner listener) {
		mType = type;
		mBackupManager = backupManager;
		mAppWidgetId = appWidgetId;
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		mListener.onRestorePreExecute(mType);
	}

	protected Integer doInBackground(Uri... uris) {
		Uri uri = uris[0];
		if (mType == PreferencesBackupManager.TYPE_INCAR) {

			if (uri == null) {
				return mBackupManager.doRestoreInCarLocal();
		 	}

			return mBackupManager.doRestoreInCarUri(uri);
		}

		if ("file".equals(uri.getScheme())) {
			return mBackupManager.doRestoreMainLocal(uri.getPath(), mAppWidgetId);
		}
		return mBackupManager.doRestoreMainUri(uri, mAppWidgetId);
	}

	@Override
	protected void onPostExecute(Integer result) {
		mListener.onRestoreFinish(mType, result);
	}



}