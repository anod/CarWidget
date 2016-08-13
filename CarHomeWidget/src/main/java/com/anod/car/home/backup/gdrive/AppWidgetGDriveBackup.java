package com.anod.car.home.backup.gdrive;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.anod.car.home.backup.PreferencesBackupManager;
import com.anod.car.home.gms.ReadDriveFileContentsAsyncTask;
import com.anod.car.home.prefs.preferences.ObjectRestoreManager;
import com.google.android.gms.drive.Metadata;

import java.io.InputStream;

/**
 * @author algavris
 * @date 13/08/2016.
 */

public class AppWidgetGDriveBackup extends GDriveBackup {
    final int mAppWidgetId;

    public AppWidgetGDriveBackup(Fragment fragment, int appWidgetId, Listener listener) {
        super(fragment, listener);
        mAppWidgetId = appWidgetId;
        if (mAppWidgetId <= 0) {
            throw new IllegalArgumentException("appWidgetId is "+appWidgetId);
        }
    }

    @Override
    protected ReadDriveFileContentsAsyncTask createReadTask() {
        return new ReadTask(mContext, mAppWidgetId, mListener);
    }

    private static class ReadTask extends ReadDriveFileContentsAsyncTask {

        private final Listener mListener;

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result.success) {
                String filename = ((DriveResult)result).metadata.getOriginalFilename();
                mListener.onGDriveDownloadFinish(filename);
            } else {
                mListener.onGDriveError();
            }
        }

        private final int mAppWidgetId;

        ReadTask(Context context, int appWidgetId, Listener listener) {
            super(context);
            mAppWidgetId = appWidgetId;
            mListener = listener;
        }

        @Override
        protected DriveResult readDriveFileBackground(InputStream inputStream, Metadata metadata) {
            PreferencesBackupManager backup = new PreferencesBackupManager(mContext);

            int result;
            if (metadata.getMimeType().equals(MIME_TYPE_OBJECT)) {
                ObjectRestoreManager objectRestore = new ObjectRestoreManager(mContext);
                result = objectRestore.doRestoreMain(inputStream, mAppWidgetId);
            } else {
                result = backup.doRestoreWidget(inputStream, mAppWidgetId);
            }

            boolean success = result == PreferencesBackupManager.RESULT_DONE;
            return new DriveResult(success, metadata);
        }
    }
}
