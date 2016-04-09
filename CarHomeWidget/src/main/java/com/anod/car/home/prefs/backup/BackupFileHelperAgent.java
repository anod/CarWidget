package com.anod.car.home.prefs.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.anod.car.home.utils.AppLog;

import java.io.File;
import java.io.IOException;


public class BackupFileHelperAgent extends BackupAgentHelper {

    /**
     * The "key" string passed when adding a helper is a token used to
     * disambiguate between entities supplied by multiple different helper
     * objects.  They only need to be unique among the helpers within this
     * one agent class, not globally unique.
     */
    static final String FILE_HELPER_KEY = "backup_incar.json";

    private PreferencesBackupManager mManager;

    /**
     * The {@link android.app.backup.FileBackupHelper FileBackupHelper} class
     * does nearly all of the work for our use case:  backup and restore of a
     * file stored within our application's getFilesDir() location.  It will
     * also handle files stored at any subpath within that location.  All we
     * need to do is a bit of one-time configuration: installing the helper
     * when this agent object is created.
     */
    @Override
    public void onCreate() {
        mManager = new PreferencesBackupManager(this);
        FileBackupHelper helper = createFileBackupHelper(this, mManager.getBackupDir(),
                PreferencesBackupManager.FILE_INCAR_JSON);
        addHelper(FILE_HELPER_KEY, helper);
    }

    /**
     * We want to ensure that the UI is not trying to rewrite the data file
     * while we're reading it for backup, so we override this method to
     * supply the necessary locking.
     */
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs the backup operation
        synchronized (PreferencesBackupManager.sLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    /**
     * Adding locking around the file rewrite that happens during restore is
     * similarly straightforward.
     */
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
            ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file from
        // the data provided here.
        synchronized (PreferencesBackupManager.sLock) {
            super.onRestore(data, appVersionCode, newState);
            mManager.doRestoreInCarLocal(mManager.getBackupIncarFile().getPath());
        }
    }

    private static FileBackupHelper createFileBackupHelper(Context context, File path,
            String file) {
        String filesDir = context.getFilesDir().getAbsolutePath();
        String absPath = path.getAbsolutePath();

        String relPath = createRelativePath(filesDir);

        StringBuilder filePathBuilder = new StringBuilder(relPath);
        filePathBuilder.append(absPath);
        filePathBuilder.append(File.separatorChar);
        filePathBuilder.append(file);

        return new FileBackupHelper(context, filePathBuilder.toString());
    }

    private static String createRelativePath(String path) {
        String[] parts = path.split(File.separator);
        StringBuilder relative = new StringBuilder("..");
        for (int i = 0; i < parts.length - 2; i++) {
            relative.append(File.separatorChar);
            relative.append("..");
        }
        return relative.toString();
    }
}
