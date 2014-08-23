package com.anod.car.home.prefs.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;

import com.anod.car.home.gms.ReadDriveFileContentsAsyncTask;
import com.anod.car.home.gms.WriteDriveFileContentsAsyncTask;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.utils.AppLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.File;
import java.io.InputStream;

/**
 * @author alex
 * @date 1/19/14
 */
public class GDriveBackup implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ConfigurationActivity.onActivityResultListener, ResultCallback<DriveApi.ContentsResult> {
	public static final int REQUEST_CODE_RESOLUTION = 123;
	public static final int REQUEST_CODE_OPENER = 122;
	public static final int REQUEST_CODE_CREATOR = 124;

	public static final String MIME_TYPE = "application/octet-stream";


	private static final int ACTION_DOWNLOAD = 1;
	private static final int ACTION_UPLOAD = 2;
	private final Listener mListener;

	private GoogleApiClient mGoogleApiClient;
	private Context mContext;
	private Activity mActivity;
	private boolean mConnected;
	private int mOnConnectAction;
	private String mTargetFileName;
	private File mFile;
	private boolean mSupported;
	private int mAppWidgetId;

	public interface Listener {
		void onGDriveActionStart();
		void onGDriveDownloadFinish();
		void onGDriveUploadFinish();
		void onGDriveError();
	}

	public GDriveBackup(Activity activity, Listener listener) {
		mContext = activity.getApplicationContext();
		mActivity = activity;
		mListener = listener;
	}

	public void connect() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(mContext)
				.addApi(Drive.API)
				.addScope(Drive.SCOPE_FILE)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		}
		mGoogleApiClient.connect();
	}

	public void disconnect() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (mOnConnectAction == ACTION_DOWNLOAD) {
			downloadConnected();
		} else if (mOnConnectAction == ACTION_UPLOAD){
			requestNewContents();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		AppLog.e("GoogleApiClient connection failed: " + result.toString());

		if (!result.hasResolution()) {
			// show the localized error dialog.
			mListener.onGDriveError();
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
			return;
		}
		try {
			result.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
		} catch (IntentSender.SendIntentException e) {
			AppLog.ex(e);
			mListener.onGDriveError();
		}
	}


	public void download(int appWidgetId) {
		mListener.onGDriveActionStart();
		mAppWidgetId = appWidgetId;
		if (!isConnected()) {
			mOnConnectAction = ACTION_DOWNLOAD;
			connect();
		} else {
			downloadConnected();
		}
	}

	private void downloadConnected() {
		IntentSender intentSender = Drive.DriveApi
			.newOpenFileActivityBuilder()
			.setMimeType(new String[]{MIME_TYPE})
			.build(mGoogleApiClient);
		try {
			mActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
		} catch (IntentSender.SendIntentException e) {
			AppLog.ex(e);
			mListener.onGDriveError();
		}
	}

	public void upload(String targetName, File file) {
		mListener.onGDriveActionStart();
		mTargetFileName = targetName;
		mFile = file;
		if (!isConnected()) {
			mOnConnectAction = ACTION_UPLOAD;
			connect();
		} else {
			requestNewContents();
		}

	}

	private void requestNewContents() {
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(this);
	}


	public boolean isConnected() {
		return mGoogleApiClient != null && mGoogleApiClient.isConnected();
	}


	public boolean checkRequestCode(int requestCode) {
		if (requestCode == REQUEST_CODE_OPENER) {
			return true;
		}
		if (requestCode == REQUEST_CODE_CREATOR) {
			return true;
		}
		if (requestCode == REQUEST_CODE_RESOLUTION) {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
			connect();
		} else if (requestCode == REQUEST_CODE_OPENER) {
			if (resultCode == Activity.RESULT_OK) {
				DriveId driveId = (DriveId) resultData.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
				new ReadTask(mContext,mAppWidgetId,mListener).execute(driveId);
			}
		} else if (requestCode == REQUEST_CODE_CREATOR) {
			if (resultCode == Activity.RESULT_OK) {
				DriveId driveId = (DriveId) resultData.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
				new WriteTask(mActivity, mListener).execute(new WriteDriveFileContentsAsyncTask.FilesParam(mFile, driveId));
			}
		}
	}

	@Override
	public void onResult(DriveApi.ContentsResult contentsResult) {
		if (!contentsResult.getStatus().isSuccess()) {
			Toast.makeText(mContext,"Error while trying to create new file contents",Toast.LENGTH_SHORT).show();
			return;
		}

		MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
				.setTitle(mTargetFileName)
				.setMimeType(MIME_TYPE)
				.build();
		IntentSender intentSender = Drive.DriveApi
				.newCreateFileActivityBuilder()
				.setInitialMetadata(metadataChangeSet)
				.setInitialContents(contentsResult.getContents())
				.build(mGoogleApiClient);
		try {
			mActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
		} catch (IntentSender.SendIntentException e) {
			AppLog.ex(e);
		}
	}

	public boolean isSupported() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS;
	}


	private static class WriteTask extends WriteDriveFileContentsAsyncTask {
		private final Listener mListener;

		public WriteTask(Context context, Listener listener) {
			super(context);
			mListener = listener;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				mListener.onGDriveUploadFinish();
			} else {
				mListener.onGDriveError();
			}
		}
	}

	private static class ReadTask extends ReadDriveFileContentsAsyncTask {
		private final Listener mListener;

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				mListener.onGDriveDownloadFinish();
			} else {
				mListener.onGDriveError();
			}
		}

		private final int mAppWidgetId;

		public ReadTask(Context context, int appWidgetId, Listener listener) {
			super(context);
			mAppWidgetId = appWidgetId;
			mListener = listener;
		}

		@Override
		protected boolean readDriveFileBackground(InputStream inputStream) {
			PreferencesBackupManager backup = new PreferencesBackupManager(mContext);

			int result = -1;
			if (mAppWidgetId == 0) {
				result = backup.doRestoreInCar(inputStream);
			} else {
				result = backup.doRestoreMain(inputStream, mAppWidgetId);
			}

			return result == PreferencesBackupManager.RESULT_DONE;
		}
	}

}
