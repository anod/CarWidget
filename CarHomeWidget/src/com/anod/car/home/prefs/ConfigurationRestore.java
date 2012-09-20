package com.anod.car.home.prefs;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.actionbarcompat.ActionBarHelper;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;

public class ConfigurationRestore extends ListActivity {
	private int mAppWidgetId;
	private PreferencesBackupManager mBackupManager;
	private Context mContext;
	private RestoreAdapter mAdapter;
	private RestoreClickListener mRestoreListener;
	private DeleteClickListener mDeleteListener;
	final private ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);
	private static final int DIALOG_WAIT = 1;

	public static final String EXTRA_TYPE = "type";
	public static final int TYPE_MAIN = 1;
	public static final int TYPE_INCAR = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mActionBarHelper.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restore_list);
		
		Intent launchIntent = getIntent();
		Bundle extras = launchIntent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			setResult(RESULT_OK);
		} else {
			finish();
		}
		mContext = (Context) this;
		mBackupManager = new PreferencesBackupManager(mContext);

		int type = extras.getInt(EXTRA_TYPE, 0);
		if (type == 0) {
			finish();
		}

		if (type == TYPE_INCAR) {
			String arg = null;
			new RestoreTask().execute(arg);
		} else {
			mRestoreListener = new RestoreClickListener();
			mDeleteListener = new DeleteClickListener();
			mAdapter = new RestoreAdapter(this, R.layout.restore_item, new ArrayList<File>());
			setListAdapter(mAdapter);
			new FileListTask().execute(0);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		mActionBarHelper.onPostCreate(savedInstanceState);
		super.onPostCreate(savedInstanceState);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean retValue = false;
        retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
        retValue |= super.onCreateOptionsMenu(menu);
        return retValue;
    }
    
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAIT:
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setCancelable(true);
			String message = getResources().getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		}
		return null;
	}

	private class FileListTask extends AsyncTask<Integer, Void, File[]> {
		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WAIT);
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
			try {
				dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {
			}

		}
	}

	private class RestoreTask extends AsyncTask<String, Void, Integer> {
		private int mTaskType;

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WAIT);
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			if (filename == null) {
				mTaskType = TYPE_INCAR;
				return mBackupManager.doRestoreInCar();
			}
			mTaskType = TYPE_MAIN;
			return mBackupManager.doRestoreMain(filename, mAppWidgetId);
		}

		protected void onPostExecute(Integer result) {
			try {
				dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {
			}
			onRestoreFinish(mTaskType, result);
		}
	}

	private void onRestoreFinish(int type, int code) {
		if (code == PreferencesBackupManager.RESULT_DONE) {
			Toast.makeText(mContext, getString(R.string.restore_done), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		switch (code) {
		case PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE:
			Toast.makeText(mContext, getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
			break;
		case PreferencesBackupManager.ERROR_DESERIALIZE:
			Toast.makeText(mContext, getString(R.string.restore_deserialize_failed), Toast.LENGTH_SHORT).show();
			break;
		case PreferencesBackupManager.ERROR_FILE_READ:
			Toast.makeText(mContext, getString(R.string.failed_to_read_file), Toast.LENGTH_SHORT).show();
			break;
		case PreferencesBackupManager.ERROR_FILE_NOT_EXIST:
			Toast.makeText(mContext, getString(R.string.backup_not_exist), Toast.LENGTH_SHORT).show();
			break;
		}
		finish();
	}

	private class RestoreAdapter extends ArrayAdapter<File> {
		private int resource;

		public RestoreAdapter(Context _context, int _resource, ArrayList<File> _items) {
			super(_context, _resource, _items);
			resource = _resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(resource, null);
			}
			File entry = getItem(position);

			TextView titleView = (TextView) v.findViewById(android.R.id.title);
			String name = entry.getName();
			name = name.substring(0, name.lastIndexOf(PreferencesBackupManager.FILE_EXT_DAT));
			titleView.setTag(name);
			titleView.setText(name);

			titleView.setOnClickListener(mRestoreListener);
			ImageView applyView = (ImageView) v.findViewById(R.id.apply_icon);
			applyView.setTag(name);
			applyView.setOnClickListener(mRestoreListener);

			ImageView deleteView = (ImageView) v.findViewById(R.id.delete_action_button);
			deleteView.setTag(entry);
			deleteView.setOnClickListener(mDeleteListener);

			v.setId(position);
			return v;
		}
	}

	private class RestoreClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			new RestoreTask().execute((String) v.getTag());
		}
	}

	private class DeleteClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			File file = (File) v.getTag();
			new DeleteTask().execute(file);
		}
	}

	private class DeleteTask extends AsyncTask<File, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WAIT);
		}

		protected Boolean doInBackground(File... files) {
			return files[0].delete();
		}

		protected void onPostExecute(Boolean result) {
			try {
				dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {
			}
			if (!result) {
				Toast.makeText(mContext, getString(R.string.unable_delete_file), Toast.LENGTH_SHORT).show();
			} else {
				new FileListTask().execute(0);
			}
		}
	}
}
