package com.anod.car.home.prefs;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.R;
import com.anod.car.home.appscache.AppsCacheAdapter;
import com.anod.car.home.appscache.AppsCacheAsyncTask;
import com.anod.car.home.appscache.AppsCacheAsyncTask.Callback;
import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;

public class AllAppsActivity extends ListActivity implements OnItemClickListener, Callback {
	private AppsListCache mAppsList;
	private ArrayList<CacheEntry> mAppsCacheEntries;
	private PackageManager mPackageManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getListView().setOnItemClickListener(this);
        setVisible(false);
        
        CarWidgetApplication app = (CarWidgetApplication)this.getApplicationContext();
        app.initAppListCache();
        
        mAppsList = app.getAppListCache();
        mAppsCacheEntries = (mAppsList == null) ? null : mAppsList.getCacheEntries();
        mPackageManager = getPackageManager();

        if (mAppsCacheEntries == null || mAppsCacheEntries.size() == 0) {
        	new AppsCacheAsyncTask(this, mAppsList, this).execute(0);
        } else {
        	showList();
        } 
    }
    
    private void showList(){
        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        AppsCacheAdapter adapter = new AppsCacheAdapter(this, R.layout.all_apps_row, mAppsCacheEntries, mAppsList);
        // Bind to our new adapter.
        setListAdapter(adapter);
        try {
        	setVisible(true);
        } catch(Exception e) {
        	Log.d("CarHomeWidget", "Cannot set list visible");
        }
    }
    
    final private Intent getActivityIntent(ComponentName className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
		CacheEntry entry = mAppsCacheEntries.get(position);

        Intent intent = getActivityIntent(entry.componentName);
        setResult(RESULT_OK, intent);
        finish();
	}

	@Override
	public void onIntentFilterInit(Intent intent) {
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
	}

	@Override
	public void onResult(ArrayList<CacheEntry> cacheEntries) {
		mAppsCacheEntries = cacheEntries;
		showList();
	}

}
