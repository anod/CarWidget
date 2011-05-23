package com.anod.car.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.AllAppsListCache.CacheEntry;

public class AllAppsActivity extends ListActivity implements OnItemClickListener {
	private AllAppsListCache mAllAppsList;
	private ArrayList<CacheEntry> mAllAppsListCache;
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getListView().setOnItemClickListener(this);
        setVisible(false);
        
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
        setVisible(true);        
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
	        v.setId(position);
	        return v;
		}
    	
    }

    private class loadAllAppsCache extends AsyncTask<Integer, Object, Object> {
    	
        @Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			mAllAppsListCache = mAllAppsList.getCacheEntries();
			showList();
		}
		@Override
		protected void onPreExecute() {
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
 
    final Intent getActivityIntent(ComponentName className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
		CacheEntry entry = mAllAppsListCache.get(position);

        Intent intent = getActivityIntent(entry.componentName);
        setResult(RESULT_OK, intent);
        finish();
	}
}
