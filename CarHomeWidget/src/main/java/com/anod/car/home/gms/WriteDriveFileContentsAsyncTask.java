package com.anod.car.home.gms;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import com.anod.car.home.utils.AppLog;

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
public class WriteDriveFileContentsAsyncTask
        extends ApiClientAsyncTask<WriteDriveFileContentsAsyncTask.FilesParam> {

    public static class FilesParam {

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
    protected Boolean doInBackgroundConnected(FilesParam... args) {
        FilesParam files = args[0];
        try {
            DriveFile target = Drive.DriveApi.getFile(getGoogleApiClient(), files.getDriveId());

            DriveApi.DriveContentsResult contentsResult = target.open(
                    getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                return false;
            }
            FileInputStream fileInputStream = new FileInputStream(files.getSource());
            InputStream inputStream = new BufferedInputStream(fileInputStream);

            DriveContents contents = contentsResult.getDriveContents();
            OutputStream outputStream = contents.getOutputStream();
            copyStream(inputStream, outputStream);

            com.google.android.gms.common.api.Status status = contents
                    .commit(getGoogleApiClient(), null).await();
            return status.getStatus().isSuccess();
        } catch (IOException e) {
            AppLog.ex(e);
        }
        return false;
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
            //showMessage("Error while editing contents");
            return;
        }
        //showMessage("Successfully edited contents");
    }
}