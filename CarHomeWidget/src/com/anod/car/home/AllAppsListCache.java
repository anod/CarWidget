package com.anod.car.home;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class AllAppsListCache {   
    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    public static class CacheEntry {
    	public ComponentName componentName;
        public Bitmap icon;
        public String title;
    }

    private final ArrayList<CacheEntry> mCache =
            new ArrayList<CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);

    private final CarWidgetApplication mContext;
    private final PackageManager mPackageManager;

    public AllAppsListCache(CarWidgetApplication context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
    }
    
    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void put(ResolveInfo info) {
        synchronized (mCache) {
        	CacheEntry entry = new CacheEntry();
        	mCache.add(entry);
        	ComponentName componentName = new ComponentName(
            	info.activityInfo.applicationInfo.packageName,
            	info.activityInfo.name
            );
        	entry.componentName = componentName;
        	entry.title = info.loadLabel(mPackageManager).toString();
        	if (entry.title == null) {
        		entry.title = info.activityInfo.name;
        	}
        	entry.icon = Utilities.createIconBitmap(
        			info.activityInfo.loadIcon(mPackageManager), mContext
            );
        }
    }

    public ArrayList<CacheEntry> getCacheEntries() {
        synchronized (mCache) {
        	return mCache;
        }
    }
}