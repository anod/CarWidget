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
import android.os.ParcelFileDescriptor;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.anod.car.home.prefs.backup.BackupCodeRender;
import com.anod.car.home.prefs.backup.BackupTask;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.prefs.backup.RestoreCodeRender;
import com.anod.car.home.prefs.backup.RestoreTask;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.CheatSheet;
import com.anod.car.home.utils.DeleteFileTask;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ConfigurationRestore extends Fragment  implements RestoreTask.RestoreTaskListner, DeleteFileTask.DeleteFileTaskListener, BackupTask.BackupTaskListner {
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
	private ExportClickListener mExportListener;

	public static final String EXTRA_TYPE = "type";

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
	private String mCurBackupFile;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.restore_list, container, false);

		mBackupMain = (ImageButton)view.findViewById(R.id.backupMain);
		mDownloadMain = (ImageButton)view.findViewById(R.id.downloadMain);

		CheatSheet.setup(mBackupMain);
		CheatSheet.setup(mDownloadMain);

		mBackupIncar = (ImageButton)view.findViewById(R.id.backupIncar);
		mDownloadIncar = (ImageButton)view.findViewById(R.id.downloadIncar);
		mUploadIncar = (ImageButton)view.findViewById(R.id.uploadIncar);
		mRestoreIncar = (ImageButton)view.findViewById(R.id.restoreIncar);

		CheatSheet.setup(mBackupIncar);
		CheatSheet.setup(mDownloadIncar);
		CheatSheet.setup(mUploadIncar);
		CheatSheet.setup(mRestoreIncar);

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

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurBackupFile = savedInstanceState.getString("curBackup");
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
			new BackupTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0, ConfigurationRestore.this).execute(null);
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
					new RestoreTask(PreferencesBackupManager.TYPE_INCAR, mBackupManager, 0, ConfigurationRestore.this).execute(null);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.apply) {
			getFragmentManager().popBackStack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate( R.menu.restore, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void setupDownloadUpload() {
		if (Utils.IS_KITKAT_OR_GREATER) {

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
					uploadFile("incar-backup"+PreferencesBackupManager.FILE_EXT_DAT, incar);
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
		outState.putString("curBackup", mCurBackupFile);
	}

	private void uploadFile(String fileName, File srcFile) {

		mCurBackupFile = srcFile.getAbsolutePath();

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
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

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

				int type = (requestCode == DOWNLOAD_MAIN_REQUEST_CODE) ? PreferencesBackupManager.TYPE_MAIN : PreferencesBackupManager.TYPE_INCAR;
				uri = resultData.getData();
				AppLog.d("Uri: " + uri.toString());
				new RestoreTask(type, mBackupManager, mAppWidgetId, ConfigurationRestore.this).execute(uri);

			}
		} else if (requestCode == UPLOAD_REQUEST_CODE && resultCode == Activity.RESULT_OK && mCurBackupFile != null) {
			Uri uri = null;
			if (resultData != null) {
				uri = resultData.getData();
				writeFile(mCurBackupFile, uri);
			}

		}
	}

	private void writeFile(String srcAbsolutePath, Uri destUri) {
		File dataFile = new File(srcAbsolutePath);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			AppLog.ex(e);
			Toast.makeText(mContext, R.string.failed_to_read_file, Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(destUri, "w");
			FileOutputStream fileOutputStream =  new FileOutputStream(pfd.getFileDescriptor());

			copyFile(inputStream, fileOutputStream);

			// Let the document provider know you're done by closing the stream.
			fileOutputStream.close();
			pfd.close();
		} catch (FileNotFoundException e) {
			AppLog.ex(e);
			Toast.makeText(mContext, R.string.failed_to_write_file, Toast.LENGTH_SHORT).show();
			return;
		} catch (IOException e) {
			AppLog.ex(e);
			Toast.makeText(mContext, R.string.failed_to_write_file, Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(mContext, R.string.export_backup_finish, Toast.LENGTH_SHORT).show();
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
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
						new BackupTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager, mAppWidgetId, ConfigurationRestore.this).execute(filename);
					}
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
	public void onRestoreFinish(int type, int code) {
		int res = RestoreCodeRender.render(code);
		Toast.makeText(mContext, res, Toast.LENGTH_SHORT).show();
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

			ImageView exportView = (ImageView) v.findViewById(R.id.uploadMain);
			if (Utils.IS_KITKAT_OR_GREATER) {
				exportView.setTag(name);
				exportView.setOnClickListener(mExportListener);
				CheatSheet.setup(exportView);
			} else {
				exportView.setVisibility(View.GONE);
			}

			CheatSheet.setup(applyView);
			CheatSheet.setup(deleteView);

			v.setId(position);
			return v;
		}
	}


	private class RestoreClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {

			Uri uri = Uri.fromFile(mBackupManager.getBackupMainFile((String) v.getTag()));
			new RestoreTask(PreferencesBackupManager.TYPE_MAIN, mBackupManager, mAppWidgetId, ConfigurationRestore.this)
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

	@Override
	public void onBackupPreExecute(int type) {
		showWaitDialog();
	}

	@Override
	public void onBackupFinish(int type, int code) {

		dismissWaitDialog();

		Resources r = getResources();
		if (code == PreferencesBackupManager.RESULT_DONE) {
			if (type == PreferencesBackupManager.TYPE_MAIN) {
				//
				new FileListTask().execute(0);
			} else if (type == PreferencesBackupManager.TYPE_INCAR) {
				updateInCarTime();
			}
		}
		int res = BackupCodeRender.render(code);
		Toast.makeText(mContext, res, Toast.LENGTH_SHORT).show();
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


	private class ExportClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			String name = (String) v.getTag();
			File file = mBackupManager.getBackupMainFile(name);
			uploadFile("car"+name+PreferencesBackupManager.FILE_EXT_DAT, file);
		}
	}
}
