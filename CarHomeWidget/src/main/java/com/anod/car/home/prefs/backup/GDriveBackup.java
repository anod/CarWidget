package com.anod.car.home.prefs.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;

import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.utils.AppLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;

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

	private GoogleApiClient mGoogleApiClient;
	private Context mContext;
	private Activity mActivity;
	private boolean mConnected;
	private int mOnConnectAction;
	private String mTargetFileName;
	private File mFile;

	public GDriveBackup(Activity activity) {
		mContext = activity.getApplicationContext();
		mActivity = activity;
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
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
			return;
		}
		try {
			result.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
		} catch (IntentSender.SendIntentException e) {
			AppLog.ex(e);
			//Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}


	public void download() {
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
		}
	}

	public void upload(String targetName, File file) {
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

	private void uploadConnected(String targetName, File file) {

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
			Uri uri = resultData.getData();
			AppLog.d("Uri: " + uri.toString());

		}
	}

	@Override
	public void onResult(DriveApi.ContentsResult contentsResult) {
		MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
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
}
