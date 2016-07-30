package com.anod.car.home.prefs.backup.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.prefs.backup.BackupCodeRender;
import com.anod.car.home.prefs.backup.BackupTask;
import com.anod.car.home.prefs.backup.GDriveBackup;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.prefs.backup.RestoreCodeRender;
import com.anod.car.home.prefs.backup.RestoreTask;
import com.anod.car.home.prefs.preferences.ObjectRestoreManager;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.CheatSheet;
import com.anod.car.home.utils.TrialDialogs;
import com.anod.car.home.utils.Version;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 30/07/2016.
 */
public class FragmentRestoreInCar extends Fragment implements RestoreTask.RestoreTaskListener,
        BackupTask.BackupTaskListener, GDriveBackup.Listener {

    @Bind(R.id.backupIncar)
    ImageButton mBackupIncar;

    @Bind(R.id.downloadIncar)
    ImageButton mDownloadIncar;

    @Bind(R.id.restoreIncar)
    ImageButton mRestoreIncar;

    @Bind(R.id.lastBackupIncar)
    TextView mLastBackupIncar;

    @Bind(R.id.uploadIncar)
    ImageButton mUploadIncar;

    private Version mVersion;
    private PreferencesBackupManager mBackupManager;
    private GDriveBackup mGDriveBackup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restore_incar, container, false);

        ButterKnife.bind(this, view);

        CheatSheet.setup(mBackupIncar);
        CheatSheet.setup(mDownloadIncar);
        CheatSheet.setup(mRestoreIncar);
        CheatSheet.setup(mUploadIncar);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBackupManager = getRestoreFragment().getBackupManager();
        mGDriveBackup = new GDriveBackup(getActivity(), this);
        ((ConfigurationActivity) getActivity()).setActivityResultListener(mGDriveBackup);

        mBackupIncar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new BackupTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0,
                        FragmentRestoreInCar.this)
                        .execute((String[]) null);
            }
        });

        mVersion = new Version(getContext());
        if (mVersion.isFree()) {
            mRestoreIncar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TrialDialogs.buildProOnlyDialog(getContext()).show();
                }
            });
        } else {
            mRestoreIncar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri;
                    if (mBackupManager.getBackupIncarFile().exists()) {
                        uri = Uri.fromFile(mBackupManager.getBackupIncarFile());
                    } else {
                        uri = Uri.fromFile(new File(mBackupManager.getBackupDir(), ObjectRestoreManager.FILE_INCAR_DAT));
                    }
                    new RestoreTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0,
                            FragmentRestoreInCar.this)
                            .execute(uri);
                }
            });
        }

        setupDownloadUpload();
        updateInCarTime();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == ConfigurationRestore.DOWNLOAD_INCAR_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                Uri uri = resultData.getData();
                AppLog.d("Uri: " + uri.toString());
                new RestoreTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0, FragmentRestoreInCar.this)
                        .execute(uri);

            }
        } else if (mGDriveBackup.checkRequestCode(requestCode)) {
            mGDriveBackup.onActivityResult(requestCode, resultCode, resultData);
        }
    }

    @Override
    public void onPause() {
        mGDriveBackup.disconnect();
        super.onPause();
    }

    private ConfigurationRestore getRestoreFragment() {
        return ((ConfigurationRestore) getParentFragment());
    }

    private void setupDownloadUpload() {
        if (mGDriveBackup.isSupported()) {

            if (mVersion.isFree()) {
                mDownloadIncar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TrialDialogs.buildProOnlyDialog(getContext()).show();
                    }
                });
            } else {
                mDownloadIncar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mGDriveBackup.download(0);
                    }
                });
            }

            mUploadIncar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File incar = mBackupManager.getBackupIncarFile();
                    mGDriveBackup.upload("incar-backup" + PreferencesBackupManager.FILE_EXT_JSON, incar);
                }
            });

        } else {
            mDownloadIncar.setVisibility(View.GONE);
            mUploadIncar.setVisibility(View.GONE);
        }
    }


    private void updateInCarTime() {
        String summary;
        long timeIncar = mBackupManager.getIncarTime();
        if (timeIncar > 0) {
            summary = DateUtils
                    .formatDateTime(getContext(), timeIncar, ConfigurationRestore.DATE_FORMAT);
        } else {
            summary = getString(R.string.never);
        }
        mLastBackupIncar.setText(summary);
    }

    @Override
    public void onBackupPreExecute(int type) {
        getRestoreFragment().startRefreshAnim();
    }

    @Override
    public void onBackupFinish(int type, int code) {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            updateInCarTime();
        }

        getRestoreFragment().stopRefreshAnim();
        int res = BackupCodeRender.render(code);
        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRestorePreExecute(int type) {
        getRestoreFragment().startRefreshAnim();
    }

    @Override
    public void onRestoreFinish(int type, int code) {
        getRestoreFragment().stopRefreshAnim();
        int res = RestoreCodeRender.render(code);
        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveActionStart() {
        getRestoreFragment().startRefreshAnim();
    }

    @Override
    public void onGDriveDownloadFinish() {
        onRestoreFinish(PreferencesBackupManager.TYPE_INCAR, PreferencesBackupManager.RESULT_DONE);
    }

    @Override
    public void onGDriveUploadFinish() {
        onBackupFinish(PreferencesBackupManager.TYPE_INCAR, PreferencesBackupManager.RESULT_DONE);
    }

    @Override
    public void onGDriveError() {
        onRestoreFinish(PreferencesBackupManager.TYPE_INCAR, PreferencesBackupManager.ERROR_UNEXPECTED);
    }
}
