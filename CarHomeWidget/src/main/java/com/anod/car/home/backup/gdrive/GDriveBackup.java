package com.anod.car.home.backup.gdrive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.anod.car.home.gms.ReadDriveFileContentsAsyncTask;
import com.anod.car.home.gms.WriteDriveFileContentsAsyncTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.File;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 1/19/14
 */
public abstract class GDriveBackup implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<DriveApi.DriveContentsResult> {

    static final int REQUEST_CODE_RESOLUTION = 123;
    static final int REQUEST_CODE_OPENER = 122;
    static final int REQUEST_CODE_CREATOR = 124;

    static final String MIME_TYPE_OBJECT = "application/octet-stream";
    static final String MIME_TYPE_JSON = "application/json";

    private static final int ACTION_DOWNLOAD = 1;
    private static final int ACTION_UPLOAD = 2;

    final Listener mListener;
    final Context mContext;
    private final Fragment mFragment;

    private GoogleApiClient mGoogleApiClient;
    private int mOnConnectAction;
    private String mTargetFileName;
    private File mFile;

    public interface Listener {
        void onGDriveActionStart();
        void onGDriveDownloadFinish(String filename);
        void onGDriveUploadFinish();
        void onGDriveError();
    }

    GDriveBackup(Fragment fragment, Listener listener) {
        mContext = fragment.getContext().getApplicationContext();
        mFragment = fragment;
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
        } else if (mOnConnectAction == ACTION_UPLOAD) {
            requestNewContents();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        AppLog.e("GoogleApiClient connection failed: " + result.toString());

        if (!result.hasResolution()) {
            // show the localized error dialog.
            mListener.onGDriveError();
            if (mFragment.getActivity() != null) {
                GoogleApiAvailability.getInstance().showErrorDialogFragment(mFragment.getActivity(), result.getErrorCode(), 0);
            }
            return;
        }
        try {
            if (mFragment.getActivity() != null) {
                result.startResolutionForResult(mFragment.getActivity(), REQUEST_CODE_RESOLUTION);
            }
        } catch (IntentSender.SendIntentException e) {
            AppLog.e(e);
            mListener.onGDriveError();
        }
    }


    public void download() {
        mListener.onGDriveActionStart();
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
                .setMimeType(new String[]{MIME_TYPE_OBJECT, MIME_TYPE_JSON})
                .build(mGoogleApiClient);
        try {
            mFragment.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            AppLog.e(e);
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
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(this);
    }


    boolean isConnected() {
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

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                connect();
            } else {
                Toast.makeText(mContext, "Error while trying to connect",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_OPENER) {
            if (resultCode == Activity.RESULT_OK) {
                mListener.onGDriveActionStart();
                DriveId driveId = resultData.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                createReadTask().execute(new ReadDriveFileContentsAsyncTask.DriveIdParams(driveId));
            }
        } else if (requestCode == REQUEST_CODE_CREATOR) {
            if (resultCode == Activity.RESULT_OK) {
                mListener.onGDriveActionStart();
                DriveId driveId = resultData.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                new WriteTask(mContext, mListener).execute(new WriteDriveFileContentsAsyncTask.FilesParam(mFile, driveId));
            }
        }
    }

    protected abstract ReadDriveFileContentsAsyncTask createReadTask();

    @Override
    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
        if (!result.getStatus().isSuccess()) {
            Toast.makeText(mContext, "Error while trying to create new file contents",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(mTargetFileName)
                .setMimeType(MIME_TYPE_JSON)
                .build();
        IntentSender intentSender = Drive.DriveApi
                .newCreateFileActivityBuilder()
                .setInitialMetadata(metadataChangeSet)
                .setInitialDriveContents(result.getDriveContents())
                .build(mGoogleApiClient);
        try {
            mFragment.startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            AppLog.e(e);
        }
    }

    public boolean isSupported() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext)
                == ConnectionResult.SUCCESS;
    }


    private static class WriteTask extends WriteDriveFileContentsAsyncTask {

        private final Listener mListener;

        WriteTask(Context context, Listener listener) {
            super(context);
            mListener = listener;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result.success) {
                mListener.onGDriveUploadFinish();
            } else {
                mListener.onGDriveError();
            }
        }
    }

}
