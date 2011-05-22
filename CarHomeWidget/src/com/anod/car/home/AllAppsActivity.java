package com.anod.car.home;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AllAppsActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        allAppsAdapter adapter = new allAppsAdapter();
        // Bind to our new adapter.
        setListAdapter(adapter);
    }
    
    
    private class allAppsAdapter extends BaseAdapter {

    	   
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    	
    }
    
    /**
     * Stores the list of all applications for the all apps view.
     */
    private class AllAppsList {
        public static final int DEFAULT_APPLICATIONS_NUMBER = 42;
        
        /** The list off all apps. */
        public ArrayList<ApplicationCacheInfo> data =
                new ArrayList<ApplicationCacheInfo>(DEFAULT_APPLICATIONS_NUMBER);

        private AllAppsIconCache mIconCache;

        /**
         * Boring constructor.
         */
        public AllAppsList(AllAppsIconCache iconCache) {
            mIconCache = iconCache;
        }

        /**
         * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
         * list to broadcast when notify() is called.
         *
         * If the app is already in the list, doesn't add it.
         */
        public void add(ApplicationCacheInfo info) {
            if (findActivity(data, info.componentName)) {
                return;
            }
            data.add(info);
        }
        
        public void clear() {
            data.clear();
            // This is more aggressive than it needs to be.
            mIconCache.flush();        
        }

        public int size() {
            return data.size();
        }

        public ApplicationCacheInfo get(int index) {
            return data.get(index);
        }

        /**
         * Add the icons for the supplied apk called packageName.
         */
        public void addPackage(Context context, String packageName) {
            final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);

            if (matches.size() > 0) {
                for (ResolveInfo info : matches) {
                    add(new ApplicationCacheInfo(info, mIconCache));
                }
            }
        }

        /**
         * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
         */
        private List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
            final PackageManager packageManager = context.getPackageManager();

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mainIntent.setPackage(packageName);

            final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
            return apps != null ? apps : new ArrayList<ResolveInfo>();
        }

        /**
         * Returns whether <em>apps</em> contains <em>component</em>.
         */
        private boolean findActivity(ArrayList<ApplicationCacheInfo> apps, ComponentName component) {
            final int N = apps.size();
            for (int i=0; i<N; i++) {
                final ApplicationCacheInfo info = apps.get(i);
                if (info.componentName.equals(component)) {
                    return true;
                }
            }
            return false;
        }

    }    
}
