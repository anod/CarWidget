package com.anod.car.home.model;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

import com.anod.car.home.CarWidgetApplication;

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

	public static class Entry {
		public ComponentName componentName;
		public Bitmap icon;
		public String title;
		public String activityName;
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
		synchronized (mCache) {
			mCache.clear();
		}
	}

	/**
	 * Fill in "application" with the icon and label for "info."
	 */
	public void put(ResolveInfo info) {
		synchronized (mCache) {
			Entry entry = new Entry();
			mCache.add(entry);
			ComponentName componentName = new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name
			);
			entry.componentName = componentName;
			entry.title = info.activityInfo.applicationInfo.loadLabel(mPackageManager).toString();
			if (entry.title == null) {
				entry.title = info.activityInfo.name;
			}
			/*
			 * entry.icon = UtilitiesBitmap.createIconBitmap(
			 * info.activityInfo.loadIcon(mPackageManager), mContext );
			 */
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