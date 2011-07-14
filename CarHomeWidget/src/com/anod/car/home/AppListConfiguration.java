package com.anod.car.home;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListConfiguration extends ListActivity {
	
	private static ArrayList<ShortcutInfo> mShortcutList = new ArrayList<ShortcutInfo>();
	private static ArrayList<String> mResult;
	private static final int REQUEST_PICK_APPLICATION = 1;
	private ShortcutListAdapter mAdapter;
	private LauncherModel mModel;
	private LayoutInflater mInflater;
	private static final String EXTRA_PACKAGE_NAMES = "extra_package_names";
	private static final int DIALOG_INIT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mModel = new LauncherModel();

		mInflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View headerView = (View)mInflater.inflate(R.layout.app_list_header, null);
		ImageButton btn = (ImageButton)headerView.findViewById(R.id.add_button);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		    	Intent mainIntent = new Intent(AppListConfiguration.this, AllAppsActivity.class);
		        startActivityForResult(mainIntent, REQUEST_PICK_APPLICATION);
			}
		});
		getListView().addHeaderView(headerView);

		mAdapter = new ShortcutListAdapter( this, R.id.title, mShortcutList);
		setListAdapter(mAdapter);
		
		mResult = getIntent().getStringArrayListExtra(EXTRA_PACKAGE_NAMES);
		if (mResult == null) {
			mResult = new ArrayList<String>();
		} else if (mResult.size() > 0) {
			new InitTask().execute(0); // todo handle activity states
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_INIT :
				ProgressDialog waitDialog = new ProgressDialog(this);
				waitDialog.setCancelable(true);
				waitDialog.setMessage(getResources().getString(R.string.please_wait));
				waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					finish();
				}
				});
			return waitDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_APPLICATION) {
			ShortcutInfo shortcut = mModel.infoFromApplicationIntent(this,data);
			mShortcutList.add(shortcut);
			mAdapter.notifyDataSetChanged();
			mResult.add(Utils.componentToString(shortcut.intent.getComponent()));
			Intent resultData = new Intent();
			data.putExtra(EXTRA_PACKAGE_NAMES, mResult);
			setResult(RESULT_OK, resultData);
		}
	}

	private class ShortcutListAdapter extends ArrayAdapter<ShortcutInfo> {

		public ShortcutListAdapter(Context context, int textViewResourceId, ArrayList<ShortcutInfo> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				rowView = (View)mInflater.inflate(R.layout.app_list_row, null);
			}
			ShortcutInfo shortcut = getItem(position);
			ImageView icon = (ImageView)rowView.findViewById(R.id.icon);
			TextView title = (TextView)rowView.findViewById(R.id.title);			
			title.setText(shortcut.title);
			icon.setImageBitmap(shortcut.getIcon());
			
			return rowView;
		}
		
	}

   private class InitTask extends AsyncTask<Integer, Integer, Boolean> { 
   	
	   	protected void onPreExecute() {
	   		showDialog(DIALOG_INIT);
	   		mShortcutList.clear();
	   		mAdapter.notifyDataSetChanged();
	   	}

	   	protected void onPostExecute(Boolean result) {
	   		try {
	   			dismissDialog(DIALOG_INIT);
	   		}  catch (IllegalArgumentException e) {
	   		}
	   	}
	    public void onProgressUpdate(Integer... values) {
	
	    }

	      @Override
	   	protected Boolean doInBackground(Integer... arg0) {
	   		initList();
	   		return true;
	   	}
   	
		private void initList() {
			Intent data = new Intent();
			for(String compString : mResult) {
				ComponentName compName = Utils.stringToComponent(compString);
				data.setComponent(compName);
				ShortcutInfo shortcut = mModel.infoFromApplicationIntent(AppListConfiguration.this,data);
				mShortcutList.add(shortcut);
				mAdapter.notifyDataSetChanged();
			}
		}

   }    

}
