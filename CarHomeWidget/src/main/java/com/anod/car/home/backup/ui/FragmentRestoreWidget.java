package com.anod.car.home.backup.ui;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.backup.BackupCodeRender;
import com.anod.car.home.backup.BackupTask;
import com.anod.car.home.backup.GDriveBackup;
import com.anod.car.home.backup.PreferencesBackupManager;
import com.anod.car.home.backup.RestoreCodeRender;
import com.anod.car.home.backup.RestoreTask;
import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.CheatSheet;
import com.anod.car.home.utils.DeleteFileTask;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 30/07/2016.
 */
public class FragmentRestoreWidget extends Fragment implements RestoreTask.RestoreTaskListener, DeleteFileTask.DeleteFileTaskListener,
        BackupTask.BackupTaskListener, GDriveBackup.Listener {

    public static FragmentRestoreWidget create(int appWidgetId) {
        FragmentRestoreWidget fragment = new FragmentRestoreWidget();
        Bundle args = new Bundle();
        args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(android.R.id.list)
    ListView mListView;

    RestoreClickListener mRestoreListener;
    DeleteClickListener mDeleteListener;
    ExportClickListener mExportListener;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    PreferencesBackupManager mBackupManager;
    RestoreAdapter mAdapter;
    GDriveBackup mGDriveBackup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restore_widget, container, false);
        ButterKnife.bind(this, view);

        mListView.setEmptyView(ButterKnife.findById(view, android.R.id.empty));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAppWidgetId = getArguments().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        mBackupManager = getRestoreFragment().getBackupManager();
        mGDriveBackup = new GDriveBackup(getActivity(), this);
        ((ConfigurationActivity) getActivity()).setActivityResultListener(mGDriveBackup);

        mRestoreListener = new RestoreClickListener();
        mDeleteListener = new DeleteClickListener();
        mExportListener = new ExportClickListener();

        mAdapter = new RestoreAdapter(getContext(), R.layout.fragment_restore_item, new ArrayList<File>());
        mListView.setAdapter(mAdapter);
        new FileListTask().execute(0);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mGDriveBackup.isSupported())
        {
            menu.findItem(R.id.menu_download_from_cloud).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_backup:
                createBackupNameDialog().show();
                return true;
            case R.id.menu_download_from_cloud:
                mGDriveBackup.download(mAppWidgetId);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == FragmentBackup.DOWNLOAD_MAIN_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                Uri uri = resultData.getData();
                AppLog.d("Uri: " + uri.toString());
                new RestoreTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager, mAppWidgetId, FragmentRestoreWidget.this)
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

    private FragmentBackup getRestoreFragment() {
        return ((FragmentBackup) getParentFragment());
    }

    @Override
    public void onBackupPreExecute(int type) {
        getRestoreFragment().startRefreshAnim();
    }

    @Override
    public void onBackupFinish(int type, int code) {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            new FileListTask().execute(0);
        }

        getRestoreFragment().stopRefreshAnim();
        int res = BackupCodeRender.render(code);
        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteFileFinish(boolean success) {
        if (!success) {
            Toast.makeText(getContext(), R.string.unable_delete_file, Toast.LENGTH_SHORT).show();
        } else {
            new FileListTask().execute(0);
        }
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

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private class RestoreAdapter extends ArrayAdapter<File> {

        private final int mResource;

        RestoreAdapter(Context context, int resource, ArrayList<File> items) {
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

            String timestamp = DateUtils.formatDateTime(getContext(), entry.lastModified(), FragmentBackup.DATE_FORMAT);
            holder.text2.setText(timestamp);

            holder.apply.setTag(name);
            holder.apply.setOnClickListener(mRestoreListener);
            CheatSheet.setup(holder.apply);

            holder.delete.setTag(entry);
            holder.delete.setOnClickListener(mDeleteListener);
            CheatSheet.setup(holder.delete);

            if (mGDriveBackup.isSupported()) {
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

    class FileListTask extends AsyncTask<Integer, Void, File[]> {

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
        getRestoreFragment().startRefreshAnim();
    }

    @Override
    public void onRestoreFinish(int type, int code) {
        getRestoreFragment().stopRefreshAnim();
        int res = RestoreCodeRender.render(code);
        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
    }

    class RestoreClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Uri uri = Uri.fromFile(mBackupManager.getBackupWidgetFile((String) v.getTag()));
            new RestoreTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager, mAppWidgetId,
                    FragmentRestoreWidget.this)
                    .execute(uri)
            ;
        }
    }

    class DeleteClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            File file = (File) v.getTag();
            new DeleteFileTask(FragmentRestoreWidget.this).execute(file);
        }
    }

    AlertDialog createBackupNameDialog() {
        String defaultFilename = "widget-" + mAppWidgetId;
        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View textEntryView = factory.inflate(R.layout.backup_dialog_enter_name, null);
        final EditText backupName = (EditText) textEntryView.findViewById(R.id.backup_name);
        backupName.setText(defaultFilename);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.backup_current_widget)
                .setView(textEntryView)
                .setPositiveButton(R.string.backup_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String filename = backupName.getText().toString();
                        if (!filename.equals("")) {
                            new BackupTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager,
                                    mAppWidgetId, FragmentRestoreWidget.this).execute(filename);
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

    class ExportClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String name = (String) v.getTag();
            File file = mBackupManager.getBackupWidgetFile(name);
            mGDriveBackup
                    .upload("car-" + name, file);
        }
    }

    @Override
    public void onGDriveActionStart() {
        getRestoreFragment().startRefreshAnim();
    }

    @Override
    public void onGDriveDownloadFinish() {
        onRestoreFinish(PreferencesBackupManager.TYPE_MAIN, PreferencesBackupManager.RESULT_DONE);
    }

    @Override
    public void onGDriveUploadFinish() {
        onBackupFinish(PreferencesBackupManager.TYPE_MAIN, PreferencesBackupManager.RESULT_DONE);
    }

    @Override
    public void onGDriveError() {
        onRestoreFinish(PreferencesBackupManager.TYPE_MAIN, PreferencesBackupManager.ERROR_UNEXPECTED);
    }
}