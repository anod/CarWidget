package com.anod.car.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.AllAppsListCache.CacheEntry;

public class AllAppsActivity extends ListActivity implements OnItemSelectedListener {
	private AllAppsListCache mAllAppsList;
	private ArrayList<CacheEntry> mAllAppsListCache;
	private static final int DIALOG_WAIT=1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getListView().setOnItemSelectedListener(this);
        
        mAllAppsList = ((CarWidgetApplication)this.getApplicationContext()).getAllAppCache();
        mAllAppsListCache = mAllAppsList.getCacheEntries();
        if (mAllAppsListCache == null || mAllAppsListCache.size() == 0) {
        	new loadAllAppsCache().execute(0);
        } else {
        	showList();
        }
    }
    
    private void showList(){
        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        allAppsAdapter adapter = new allAppsAdapter(this, R.layout.all_apps_row, mAllAppsListCache);
        // Bind to our new adapter.
        setListAdapter(adapter);
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

    
    private class allAppsAdapter extends ArrayAdapter<CacheEntry> {
    	private int resource;
    	
    	public allAppsAdapter(Context _context, int _resource, List<CacheEntry> _items) {
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
			CacheEntry entry = getItem(position);

			TextView title = (TextView) v.findViewById(R.id.app_title);
	        ImageView icon = (ImageView) v.findViewById(R.id.app_icon);
	        title.setText(entry.title);
	        icon.setImageBitmap(entry.icon);
	        return v;
		}
    	
    }

    private class loadAllAppsCache extends AsyncTask<Integer, Object, Object> {
    	
        @Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			mAllAppsListCache = mAllAppsList.getCacheEntries();
			showList();
			try{
				dismissDialog(DIALOG_WAIT);
			} catch (Exception e) {}
		}
		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WAIT);
			super.onPreExecute();
		}
		protected Object doInBackground(Integer... param) {
        	loadAllAppsToCache();
        	return null;
        }
        private void loadAllAppsToCache() {
        	mAllAppsList.flush();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager packageManager = getPackageManager();
            List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
            Collections.sort(apps,new ResolveInfo.DisplayNameComparator(packageManager));
            for(ResolveInfo appInfo : apps) {
            	mAllAppsList.put(appInfo);
            } 
        }
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
        
}
