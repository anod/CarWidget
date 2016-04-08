package com.anod.car.home.model;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.app.AppIconLoader;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Cache of application icons. Icons can be made from any thread.
 */
public class AppsList {

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private final ArrayList<Entry> mCache = new ArrayList<Entry>(INITIAL_ICON_CACHE_CAPACITY);

    private final CarWidgetApplication mContext;

    private final PackageManager mPackageManager;

    private AppIconLoader mAppIconLoader;

    public AppIconLoader getAppIconLoader() {
        if (mAppIconLoader == null) {
            mAppIconLoader = new AppIconLoader(mContext);
        }
        return mAppIconLoader;
    }

    public static class Entry {

        public ComponentName componentName;

        public int iconRes;

        public String title;
    }


    public AppsList(CarWidgetApplication context) {
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
        if (mAppIconLoader != null) {
            mAppIconLoader.shutdown();
        }
        synchronized (mCache) {
            mCache.clear();
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void put(ResolveInfo info, String title) {
        synchronized (mCache) {
            Entry entry = new Entry();
            mCache.add(entry);
            ComponentName componentName = new ComponentName(
                    info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name
            );
            entry.componentName = componentName;
            entry.title = title;
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }
        }
    }

    public void sort() {
        Collections.sort(mCache, new DisplayNameComparator());
    }

    public static class DisplayNameComparator implements Comparator<Entry> {

        private final Collator sCollator = Collator.getInstance();

        public final int compare(Entry a, Entry b) {
            return sCollator.compare(a.title, b.title);
        }

    }

    public ArrayList<Entry> getEntries() {
        synchronized (mCache) {
            return mCache;
        }
    }


}