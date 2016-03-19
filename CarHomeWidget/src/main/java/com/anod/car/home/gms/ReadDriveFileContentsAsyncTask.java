package com.anod.car.home.gms;

import android.content.Context;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.InputStream;

/**
 * @author alex
 * @date 2/28/14
 */
public abstract class ReadDriveFileContentsAsyncTask
        extends ApiClientAsyncTask<DriveId> {

    public ReadDriveFileContentsAsyncTask(Context context) {
        super(context);
    }

    @Override
    protected Boolean doInBackgroundConnected(DriveId... params) {

        DriveId driveId = params[0];
        DriveFile file = driveId.asDriveFile();
        DriveApi.DriveContentsResult contentsResult = file
                .open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            return null;
        }

        DriveContents contents = contentsResult.getDriveContents();

        InputStream inputStream = contents.getInputStream();
        boolean result = readDriveFileBackground(inputStream);

        contents.discard(getGoogleApiClient());
        return result;
    }

    protected abstract boolean readDriveFileBackground(InputStream inputStream);

}