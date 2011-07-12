package com.anod.car.home;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AppList extends ListActivity {
	public static ArrayList<ShortcutInfo> sList = new ArrayList<ShortcutInfo>();
	private static final int REQUEST_PICK_APPLICATION = 1;
	private ShortcutListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources r = getResources();
		LayoutInflater li = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View headerView = (View)li.inflate(R.layout.app_list_header, null);
		ImageButton btn = (ImageButton)headerView.findViewById(R.id.add_button);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		    	Intent mainIntent = new Intent(AppList.this, AllAppsActivity.class);
		        startActivityForResult(mainIntent, REQUEST_PICK_APPLICATION);
			}
		});
		getListView().addHeaderView(headerView);

		mAdapter = new ShortcutListAdapter( this, R.id.title, sList);
		setListAdapter(mAdapter);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_APPLICATION) {
			LauncherModel model = new LauncherModel();
			ShortcutInfo shortcut = model.infoFromApplicationIntent(this,data);
			sList.add(shortcut);
			mAdapter.notifyDataSetChanged();
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
				LayoutInflater li = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				rowView = (View)li.inflate(R.layout.app_list_row, null);
			}
			ShortcutInfo shortcut = getItem(position);
			ImageView icon = (ImageView)rowView.findViewById(R.id.icon);
			TextView title = (TextView)rowView.findViewById(R.id.title);			
			title.setText(shortcut.title);
			icon.setImageBitmap(shortcut.getIcon());
			
			return rowView;
		}
		
	}
}
