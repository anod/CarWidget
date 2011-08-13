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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.car.home.R;

public class ConfigurationRestore extends ListActivity {
    private int mAppWidgetId; 
	private BackupManager mBackupManager;
	private Context mContext;
	private RestoreAdapter mAdapter;
	private RestoreClickListener mRestoreListener;
	private DeleteClickListener mDeleteListener;
	private static final int DIALOG_WAIT=1;
	
	public static final String EXTRA_TYPE = "type";	
	public static final int TYPE_MAIN = 1;
	public static final int TYPE_INCAR = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            setResult(RESULT_OK);
        } else {
            finish();
        }
        mContext = (Context)this;       
		mBackupManager = new BackupManager(mContext);
       	
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
			TextView emptyView = new TextView(this);
			emptyView.setText("No backup files found.");
			getListView().setEmptyView(emptyView);
			new FileListTask().execute(0);			
		}
	}

    @Override
    public Dialog onCreateDialog(int id) {
    	switch(id) {
	    	case DIALOG_WAIT :
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
		 }

		 protected File[] doInBackground(Integer... params) {
	    	 return  mBackupManager.getMainBackups();
	     }

	     protected void onPostExecute(File[] result) {
	    	if (result!=null) {
	    		for(int i=0;i<result.length;i++) {
	    			mAdapter.add(result[i]);
	    			mAdapter.notifyDataSetChanged();
	    		}
	    	}
	     	try {
	    		dismissDialog(DIALOG_WAIT);
	    	} catch (IllegalArgumentException e) { }
	    	
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
	    	} catch (IllegalArgumentException e) { }
	    	 onRestoreFinish(mTaskType, result);
	     }
	}

	private void onRestoreFinish(int type, int code) {
		if (code == BackupManager.RESULT_DONE) {
			Toast.makeText(mContext, "Restore is done.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		switch (code) {
			case BackupManager.ERROR_STORAGE_NOT_AVAILABLE:
				Toast.makeText(mContext, "External storage is not avialable", Toast.LENGTH_SHORT).show();
			break;
			case BackupManager.ERROR_DESERIALIZE:
				Toast.makeText(mContext, "Failed to deserialize backup", Toast.LENGTH_SHORT).show();		
			break;
			case BackupManager.ERROR_FILE_READ:
            	Toast.makeText(mContext, "BackupManager failed to read the file", Toast.LENGTH_SHORT).show();
            break;
			case BackupManager.ERROR_FILE_NOT_EXIST:
	            Toast.makeText(mContext, "Backup file is not exists", Toast.LENGTH_SHORT).show();
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
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(resource, null);
			}
			File entry = getItem(position);
			
			TextView titleView = (TextView)v.findViewById(android.R.id.title);
			String name = entry.getName();
			name = name.substring(0, name.lastIndexOf(BackupManager.FILE_EXT_DAT));
			titleView.setTag(name);
			titleView.setText(name);

			titleView.setOnClickListener(mRestoreListener);
			ImageView applyView = (ImageView)v.findViewById(R.id.apply_icon);
			applyView.setTag(name);
			applyView.setOnClickListener(mRestoreListener);
			
			ImageView deleteView = (ImageView)v.findViewById(R.id.delete_action_button);
			deleteView.setTag(name);
			deleteView.setOnClickListener(mDeleteListener);
			
	        v.setId(position);
	        return v;
		}
    }
    
    private class RestoreClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			new RestoreTask().execute((String)v.getTag());					
		}
    }
    
    private class DeleteClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
							
		}
    }
}
