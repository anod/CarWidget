package com.anod.car.home.gms;

import android.content.Context;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

import java.io.InputStream;

/**
 * @author alex
 * @date 2/28/14
 */
public abstract class ReadDriveFileContentsAsyncTask
        extends ApiClientAsyncTask {

    public ReadDriveFileContentsAsyncTask(Context context) {
        super(context);
    }

    @Override
    protected Boolean doInBackgroundConnected(Params params) {
        DriveId driveId = ((DriveIdParams) params).driveId;
        DriveFile file = driveId.asDriveFile();
        DriveApi.DriveContentsResult contentsResult = file
                .open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            return null;
        }
        DriveResource.MetadataResult metadataResult = file.getMetadata(getGoogleApiClient()).await();

        DriveContents contents = contentsResult.getDriveContents();

        InputStream inputStream = contents.getInputStream();
        boolean result = readDriveFileBackground(inputStream, metadataResult.getMetadata());

        contents.discard(getGoogleApiClient());
        return result;
    }

    protected abstract boolean readDriveFileBackground(InputStream inputStream, Metadata metadata);

    public static class DriveIdParams extends Params {
        private final DriveId driveId;

        public DriveIdParams(DriveId driveId) {
            this.driveId = driveId;
        }
    }
}