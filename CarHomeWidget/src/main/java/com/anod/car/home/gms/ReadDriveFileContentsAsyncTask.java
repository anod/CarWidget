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
public abstract class ReadDriveFileContentsAsyncTask extends ApiClientAsyncTask {

    public static class DriveIdParams extends Params {
        final DriveId driveId;

        public DriveIdParams(DriveId driveId) {
            this.driveId = driveId;
        }
    }

    public static class DriveResult extends Result
    {
        public final Metadata metadata;

        public DriveResult(boolean success, Metadata metadata) {
            super(success);
            this.metadata = metadata;
        }
    }

    public ReadDriveFileContentsAsyncTask(Context context) {
        super(context);
    }

    @Override
    protected Result doInBackgroundConnected(Params params) {
        DriveId driveId = ((DriveIdParams) params).driveId;
        DriveFile file = driveId.asDriveFile();
        DriveApi.DriveContentsResult contentsResult = file
                .open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            return Result.FALSE;
        }
        DriveResource.MetadataResult metadataResult = file.getMetadata(getGoogleApiClient()).await();
        DriveContents contents = contentsResult.getDriveContents();

        InputStream inputStream = contents.getInputStream();
        DriveResult result = readDriveFileBackground(inputStream, metadataResult.getMetadata());

        contents.discard(getGoogleApiClient());
        return result;
    }

    protected abstract DriveResult readDriveFileBackground(InputStream inputStream, Metadata metadata);

}