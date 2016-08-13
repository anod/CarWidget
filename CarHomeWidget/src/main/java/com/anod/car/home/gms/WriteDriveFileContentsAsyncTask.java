package com.anod.car.home.gms;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import info.anodsplace.android.log.AppLog;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author alex
 * @date 2/28/14
 */
public class WriteDriveFileContentsAsyncTask extends ApiClientAsyncTask {

    public static class FilesParam extends Params {
        private final File mSourceFile;
        private final DriveId mDriveId;

        public FilesParam(File sourceFile, DriveId driveId) {
            mSourceFile = sourceFile;
            mDriveId = driveId;
        }

        public DriveId getDriveId() {
            return mDriveId;
        }

        public File getSource() {
            return mSourceFile;
        }
    }

    public WriteDriveFileContentsAsyncTask(Context context) {
        super(context);
    }

    @Override
    protected Result doInBackgroundConnected(Params params) {
        FilesParam files = (FilesParam) params;
        try {
            DriveFile target = files.getDriveId().asDriveFile();

            DriveApi.DriveContentsResult contentsResult = target.open(
                    getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                return Result.FALSE;
            }
            FileInputStream fileInputStream = new FileInputStream(files.getSource());
            InputStream inputStream = new BufferedInputStream(fileInputStream);

            DriveContents contents = contentsResult.getDriveContents();
            OutputStream outputStream = contents.getOutputStream();
            copyStream(inputStream, outputStream);

            com.google.android.gms.common.api.Status status = contents
                    .commit(getGoogleApiClient(), null).await();

            return new Result(status.getStatus().isSuccess());
        } catch (IOException e) {
            AppLog.e(e);
        }
        return Result.FALSE;
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}