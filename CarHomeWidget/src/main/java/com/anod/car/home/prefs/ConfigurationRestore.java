package com.anod.car.home.prefs;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.prefs.backup.BackupCodeRender;
import com.anod.car.home.prefs.backup.BackupTask;
import com.anod.car.home.prefs.backup.GDriveBackup;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.prefs.backup.RestoreCodeRender;
import com.anod.car.home.prefs.backup.RestoreTask;
import com.anod.car.home.prefs.preferences.ObjectRestoreManager;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.CheatSheet;
import com.anod.car.home.utils.DeleteFileTask;
import com.anod.car.home.utils.TrialDialogs;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConfigurationRestore extends Fragment implements
        RestoreTask.RestoreTaskListener, DeleteFileTask.DeleteFileTaskListener,
        BackupTask.BackupTaskListner, GDriveBackup.Listener {

    private static final int DOWNLOAD_MAIN_REQUEST_CODE = 1;
    private static final int DOWNLOAD_INCAR_REQUEST_CODE = 2;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private PreferencesBackupManager mBackupManager;
    private Context mContext;
    private RestoreAdapter mAdapter;
    private RestoreClickListener mRestoreListener;
    private DeleteClickListener mDeleteListener;
    private ExportClickListener mExportListener;
    public static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_WEEKDAY
            | DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_YEAR
            | DateUtils.FORMAT_ABBREV_ALL;

    @Bind(R.id.backupMain)
    ImageButton mBackupMain;

    @Bind(R.id.backupIncar)
    ImageButton mBackupIncar;

    @Bind(android.R.id.list)
    ListView mListView;

    @Bind(R.id.downloadMain)
    ImageButton mDownloadMain;

    @Bind(R.id.downloadIncar)
    ImageButton mDownloadIncar;

    @Bind(R.id.uploadIncar)
    ImageButton mUploadIncar;

    @Bind(R.id.restoreIncar)
    ImageButton mRestoreIncar;

    @Bind(R.id.lastBackupIncar)
    TextView mLastBackupIncar;

    private String mLastBackupStr;

    private MenuItem mRefreshMenuItem;

    private Version mVersion;

    private GDriveBackup mGDriveBackup;


    private boolean mIsGDriveSupported;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.restore_list, container, false);

        ButterKnife.bind(this, view);

        CheatSheet.setup(mBackupMain);
        CheatSheet.setup(mDownloadMain);
        CheatSheet.setup(mBackupIncar);
        CheatSheet.setup(mDownloadIncar);
        CheatSheet.setup(mUploadIncar);
        CheatSheet.setup(mRestoreIncar);

        mListView.setEmptyView(ButterKnife.findById(view, android.R.id.empty));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getActivity().getIntent());
        super.onActivityCreated(savedInstanceState);

        if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Intent defaultResultValue = new Intent();
            defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            getActivity().setResult(Activity.RESULT_OK, defaultResultValue);
        } else {
            AppLog.w("AppWidgetId required");
            getActivity().finish();
            return;
        }

        mContext = getActivity();

        mBackupManager = new PreferencesBackupManager(mContext);

        mGDriveBackup = new GDriveBackup(getActivity(), this);
        mIsGDriveSupported = mGDriveBackup.isSupported();

        ((ConfigurationActivity) getActivity()).setActivityResultListener(mGDriveBackup);

        mLastBackupStr = getString(R.string.last_backup);

        mBackupMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBackupNameDialog().show();
            }
        });

        mBackupIncar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String snull = null;
                new BackupTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0,
                        ConfigurationRestore.this)
                        .execute(snull);
            }
        });

        mVersion = new Version(mContext);
        if (mVersion.isFree()) {
            mRestoreIncar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TrialDialogs.buildProOnlyDialog(mContext).show();
                }
            });
        } else {
            mRestoreIncar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri;
                    if (mBackupManager.getBackupIncarFile().exists()){
                        uri = Uri.fromFile(mBackupManager.getBackupIncarFile());
                    } else {
                        uri = Uri.fromFile(new File(mBackupManager.getBackupDir(), ObjectRestoreManager.FILE_INCAR_DAT));
                    }
                    new RestoreTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0,
                            ConfigurationRestore.this)
                            .execute(uri);
                }
            });
        }

        setupDownloadUpload();

        mRestoreListener = new RestoreClickListener();
        mDeleteListener = new DeleteClickListener();
        mExportListener = new ExportClickListener();
        mAdapter = new RestoreAdapter(mContext, R.layout.restore_item, new ArrayList<File>());
        mListView.setAdapter(mAdapter);

        new FileListTask().execute(0);

        updateInCarTime();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.restore, menu);

        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
        mRefreshMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * stop refresh button animation
     */
    private void stopRefreshAnim() {
        if (mRefreshMenuItem == null) {
            return;
        }
        View actionView = mRefreshMenuItem.getActionView();
        if (actionView != null) {
            actionView.clearAnimation();
            mRefreshMenuItem.setActionView(null);
        }
        mRefreshMenuItem.setVisible(false);
    }

    /**
     * Animate refresh button
     */
    private void startRefreshAnim() {
        if (mRefreshMenuItem == null) {
            return;
        }
        View actionView = mRefreshMenuItem.getActionView();
        //already animating
        if (actionView != null) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

        Animation rotation = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        mRefreshMenuItem.setVisible(true);
        mRefreshMenuItem.setActionView(iv);

    }

    private void setupDownloadUpload() {
        if (mIsGDriveSupported) {

            mDownloadMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGDriveBackup.download(mAppWidgetId);
                }
            });

            if (mVersion.isFree()) {
                mDownloadIncar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TrialDialogs.buildProOnlyDialog(mContext).show();
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
            mDownloadMain.setVisibility(View.GONE);
            mDownloadIncar.setVisibility(View.GONE);
            mUploadIncar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if ((requestCode == DOWNLOAD_MAIN_REQUEST_CODE
                || requestCode == DOWNLOAD_INCAR_REQUEST_CODE)
                && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {

                int type = (requestCode == DOWNLOAD_MAIN_REQUEST_CODE)
                        ? PreferencesBackupManager.TYPE_MAIN : PreferencesBackupManager.TYPE_INCAR;
                uri = resultData.getData();
                AppLog.d("Uri: " + uri.toString());
                new RestoreTask(type, mBackupManager, mAppWidgetId, ConfigurationRestore.this)
                        .execute(uri);

            }
        } else if (mGDriveBackup.checkRequestCode(requestCode)) {
            mGDriveBackup.onActivityResult(requestCode, resultCode, resultData);
        }
    }

    private AlertDialog createBackupNameDialog() {
        String defaultFilename = "widget-" + mAppWidgetId;
        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.backup_dialog_enter_name, null);
        final EditText backupName = (EditText) textEntryView.findViewById(R.id.backup_name);
        backupName.setText(defaultFilename);

        return new AlertDialog.Builder(mContext)
                .setTitle(R.string.backup_current_widget)
                .setView(textEntryView)
                .setPositiveButton(R.string.backup_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String filename = backupName.getText().toString();
                        if (!filename.equals("")) {
                            new BackupTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager,
                                    mAppWidgetId, ConfigurationRestore.this).execute(filename);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Nothing
                    }
                }).create()
                ;
    }

    @Override
    public void onDeleteFileFinish(boolean success) {
        if (!success) {
            Toast.makeText(mContext, R.string.unable_delete_file, Toast.LENGTH_SHORT).show();
        } else {
            new FileListTask().execute(0);
        }
    }

    @Override
    public void onPause() {
        mGDriveBackup.disconnect();
        stopRefreshAnim();
        super.onPause();
    }

    @Override
    public void onGDriveActionStart() {
        startRefreshAnim();
    }

    @Override
    public void onGDriveDownloadFinish() {
        onRestoreFinish(0, PreferencesBackupManager.RESULT_DONE);
    }

    @Override
    public void onGDriveUploadFinish() {
        onBackupFinish(0, PreferencesBackupManager.RESULT_DONE);
    }

    @Override
    public void onGDriveError() {
        onRestoreFinish(0, PreferencesBackupManager.ERROR_UNEXPECTED);
    }


    private class FileListTask extends AsyncTask<Integer, Void, File[]> {

        @Override
        protected void onPreExecute() {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        }

        protected File[] doInBackground(Integer... params) {
            return mBackupManager.getMainBackups();
        }

        protected void onPostExecute(File[] result) {
            if (result != null) {
                for (int i = 0; i < result.length; i++) {
                    mAdapter.add(result[i]);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    @Override
    public void onRestorePreExecute(int type) {
        startRefreshAnim();
    }

    @Override
    public void onRestoreFinish(int type, int code) {
        stopRefreshAnim();
        int res = RestoreCodeRender.render(code);
        Toast.makeText(mContext, res, Toast.LENGTH_SHORT).show();
    }

    static class ViewHolder {

        @Bind(android.R.id.title)
        TextView title;

        @Bind(android.R.id.text2)
        TextView text2;

        @Bind(R.id.apply_icon)
        ImageView apply;

        @Bind(R.id.delete_button)
        ImageView delete;

        @Bind(R.id.uploadMain)
        ImageView export;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private class RestoreAdapter extends ArrayAdapter<File> {

        private final int mResource;

        public RestoreAdapter(Context context, int resource, ArrayList<File> items) {
            super(context, resource, items);
            mResource = resource;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(mResource, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            File entry = getItem(position);

            String name = entry.getName();
            String title = name.substring(0, name.lastIndexOf("."));
            holder.title.setTag(entry.getName());
            holder.title.setText(title);
            holder.title.setOnClickListener(mRestoreListener);

            String timestamp = DateUtils.formatDateTime(mContext, entry.lastModified(), DATE_FORMAT);
            holder.text2.setText(timestamp);

            holder.apply.setTag(name);
            holder.apply.setOnClickListener(mRestoreListener);
            CheatSheet.setup(holder.apply);

            holder.delete.setTag(entry);
            holder.delete.setOnClickListener(mDeleteListener);
            CheatSheet.setup(holder.delete);

            if (mIsGDriveSupported) {
                holder.export.setTag(name);
                holder.export.setOnClickListener(mExportListener);
                CheatSheet.setup(holder.export);
            } else {
                holder.export.setVisibility(View.GONE);
            }

            view.setId(position);
            return view;
        }
    }


    private class RestoreClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Uri uri = Uri.fromFile(mBackupManager.getBackupWidgetFile((String) v.getTag()));
            new RestoreTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager, mAppWidgetId,
                    ConfigurationRestore.this)
                    .execute(uri)
            ;
        }
    }

    private class DeleteClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            File file = (File) v.getTag();
            new DeleteFileTask(ConfigurationRestore.this).execute(file);
        }
    }


    @Override
    public void onBackupPreExecute(int type) {
        startRefreshAnim();
    }

    @Override
    public void onBackupFinish(int type, int code) {

        if (code == PreferencesBackupManager.RESULT_DONE) {
            if (type == PreferencesBackupManager.TYPE_MAIN) {
                //
                new FileListTask().execute(0);
            } else if (type == PreferencesBackupManager.TYPE_INCAR) {
                updateInCarTime();
            }
        }

        stopRefreshAnim();
        int res = BackupCodeRender.render(code);
        Toast.makeText(mContext, res, Toast.LENGTH_SHORT).show();
    }

    private void updateInCarTime() {
        String summary;
        long timeIncar = mBackupManager.getIncarTime();
        if (timeIncar > 0) {
            summary = DateUtils
                    .formatDateTime(mContext, timeIncar, DATE_FORMAT);
        } else {
            summary = getString(R.string.never);
        }
        mLastBackupIncar.setText(String.format(mLastBackupStr, summary));
    }


    private class ExportClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String name = (String) v.getTag();
            File file = mBackupManager.getBackupWidgetFile(name);
            mGDriveBackup.upload("car" + name + PreferencesBackupManager.FILE_EXT_JSON, file);
        }
    }
}
