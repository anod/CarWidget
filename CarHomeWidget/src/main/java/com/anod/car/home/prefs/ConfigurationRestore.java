package com.anod.car.home.prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.app.ActionBarListActivity;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

import java.io.File;
import java.util.ArrayList;

public class ConfigurationRestore extends Fragment {
	private static final int DOWNLOAD_MAIN_REQUEST_CODE = 1;
	private static final int DOWNLOAD_INCAR_REQUEST_CODE = 2;
	private static final int UPLOAD_REQUEST_CODE = 3;
	public static final String MIME_TYPE = "application/octet-stream";

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private PreferencesBackupManager mBackupManager;
	private Context mContext;
	private RestoreAdapter mAdapter;
	private RestoreClickListener mRestoreListener;
	private DeleteClickListener mDeleteListener;

	public static final String EXTRA_TYPE = "type";
	public static final int TYPE_MAIN = 1;
	public static final int TYPE_INCAR = 2;
	private ImageButton mBackupMain;
	private ImageButton mBackupIncar;
	private ListView mListView;
	private ImageButton mDownloadMain;
	private ImageButton mDownloadIncar;
	private ImageButton mUploadIncar;
	private ImageButton mRestoreIncar;
	private ProgressDialog mWaitDialog;
	private String mLastBackupStr;
	private TextView mLastBackupIncar;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.restore_list, container, false);

		mBackupMain = (ImageButton)view.findViewById(R.id.backupMain);
		mDownloadMain = (ImageButton)view.findViewById(R.id.downloadMain);

		mBackupIncar = (ImageButton)view.findViewById(R.id.backupIncar);
		mDownloadIncar = (ImageButton)view.findViewById(R.id.downloadIncar);
		mUploadIncar = (ImageButton)view.findViewById(R.id.uploadIncar);
		mRestoreIncar = (ImageButton)view.findViewById(R.id.restoreIncar);

		mLastBackupIncar = (TextView)view.findViewById(R.id.lastBackupIncar);

		mListView = (ListView)view.findViewById(android.R.id.list);
		mListView.setEmptyView(view.findViewById(android.R.id.empty));
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getActivity().getIntent());
		super.onCreate(savedInstanceState);

		if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			Intent defaultResultValue = new Intent();
			defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			((ConfigurationActivity)getActivity()).setAppWidgetId(mAppWidgetId);
			getActivity().setResult(Activity.RESULT_OK, defaultResultValue);
		} else {
			AppLog.w("AppWidgetId required");
			getActivity().finish();
			return;
		}

		mContext = (Context) getActivity();
		mBackupManager = new PreferencesBackupManager(mContext);

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
				new BackupTask().execute(null);
			}
		});

		Version version = new Version(mContext);
		if (version.isFree()) {
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
					new RestoreTask().execute(null);
				}
			});
		}

		mDownloadMain.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType(MIME_TYPE);
				startActivityForResult(intent, DOWNLOAD_MAIN_REQUEST_CODE);
			}
		});

		mDownloadIncar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType(MIME_TYPE);
				startActivityForResult(intent, DOWNLOAD_INCAR_REQUEST_CODE);
			}
		});

		mUploadIncar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				File incar = mBackupManager.getBackupIncarFile();
				uploadFile(incar.getAbsolutePath());
			}
		});

		mRestoreListener = new RestoreClickListener();
		mDeleteListener = new DeleteClickListener();
		mAdapter = new RestoreAdapter(mContext, R.layout.restore_item, new ArrayList<File>());
		mListView.setAdapter(mAdapter);

		new FileListTask().execute(0);

		updateInCarTime();

		setHasOptionsMenu(true);
	}

	private void uploadFile(String fileName) {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

		// Filter to only show results that can be "opened", such as
		// a file (as opposed to a list of contacts or timezones).
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		// Create a file with the requested MIME type.
		intent.setType(MIME_TYPE);
		intent.putExtra(Intent.EXTRA_TITLE, fileName);
		startActivityForResult(intent, UPLOAD_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
								 Intent resultData) {

		// The ACTION_OPEN_DOCUMENT intent was sent with the request code
		// READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
		// response to some other intent, and the code below shouldn't run at all.

		if ((requestCode == DOWNLOAD_MAIN_REQUEST_CODE || requestCode == DOWNLOAD_INCAR_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
			// The document selected by the user won't be returned in the intent.
			// Instead, a URI to that document will be contained in the return intent
			// provided to this method as a parameter.
			// Pull that URI using resultData.getData().
			Uri uri = null;
			if (resultData != null) {
				uri = resultData.getData();
				AppLog.d("Uri: " + uri.toString());
			}
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
							new BackupTask().execute(filename);
						}
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//Nothing
					}
				}).create();
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


	private class RestoreTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			if (filename == null) {
				return mBackupManager.doRestoreInCar();
			}
			return mBackupManager.doRestoreMain(filename, mAppWidgetId);
		}

		protected void onPostExecute(Integer result) {
			onRestoreFinish(result);
		}
	}

	private void onRestoreFinish(int code) {
		if (code == PreferencesBackupManager.RESULT_DONE) {
			Toast.makeText(mContext, getString(R.string.restore_done), Toast.LENGTH_SHORT).show();
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
		default:
		}
	}

	private class RestoreAdapter extends ArrayAdapter<File> {
		private final int mResource;

		public RestoreAdapter(Context context, int resource, ArrayList<File> items) {
			super(context, resource, items);
			mResource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(mResource, null);
			}
			File entry = getItem(position);

			TextView titleView = (TextView) v.findViewById(android.R.id.title);
			String name = entry.getName();
			name = name.substring(0, name.lastIndexOf(PreferencesBackupManager.FILE_EXT_DAT));
			titleView.setTag(name);
			titleView.setText(name);

			TextView text1 = (TextView)v.findViewById(android.R.id.text2);
			String timestamp = DateUtils.formatDateTime(mContext, entry.lastModified(), PreferencesBackupManager.DATE_FORMAT);
			text1.setText(timestamp);
			
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
		}

		protected Boolean doInBackground(File... files) {
			return files[0].delete();
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Toast.makeText(mContext, getString(R.string.unable_delete_file), Toast.LENGTH_SHORT).show();
			} else {
				new FileListTask().execute(0);
			}
		}
	}

	public class BackupTask extends AsyncTask<String, Void, Integer> {
		private int mTaskType;

		@Override
		protected void onPreExecute() {
			showWaitDialog();
		}

		protected Integer doInBackground(String... filenames) {
			String filename = filenames[0];
			if (filename == null) {
				mTaskType = TYPE_INCAR;
				return mBackupManager.doBackupInCar();
			}
			mTaskType = TYPE_MAIN;
			return mBackupManager.doBackupMain(filename, mAppWidgetId);
		}

		protected void onPostExecute(Integer result) {
			dismissWaitDialog();
			onBackupFinish(mTaskType, result);
		}
	}

	private void dismissWaitDialog() {
		if (mWaitDialog != null) {
			try {
				mWaitDialog.dismiss();
			} catch (Exception e) {
				//
			}
		}
	}

	private void showWaitDialog() {
		if (mWaitDialog == null) {
			mWaitDialog = createWaitDialog();
		}
		mWaitDialog.show();
	}

	private void onBackupFinish(int type, int code) {
		Resources r = getResources();
		if (code == PreferencesBackupManager.RESULT_DONE) {
			if (type == TYPE_MAIN) {
				//
				new FileListTask().execute(0);
			} else if (type == TYPE_INCAR) {
				updateInCarTime();
			}
			Toast.makeText(mContext, r.getString(R.string.backup_done), Toast.LENGTH_SHORT).show();
			return;
		}
		if (code == PreferencesBackupManager.ERROR_STORAGE_NOT_AVAILABLE) {
			Toast.makeText(mContext, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
		} else if (code == PreferencesBackupManager.ERROR_FILE_WRITE) {
			Toast.makeText(mContext, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
		}
	}

	protected ProgressDialog createWaitDialog() {
		ProgressDialog waitDialog = new ProgressDialog(mContext);
		waitDialog.setCancelable(true);
		String message = getResources().getString(R.string.please_wait);
		waitDialog.setMessage(message);
		return waitDialog;
	}

	private void updateInCarTime() {
		String summary;
		long timeIncar = mBackupManager.getIncarTime();
		if (timeIncar > 0) {
			summary = DateUtils.formatDateTime(mContext, timeIncar, PreferencesBackupManager.DATE_FORMAT);
		} else {
			summary = getString(R.string.never);
		}
		mLastBackupIncar.setText(String.format(mLastBackupStr, summary));
	}


}
