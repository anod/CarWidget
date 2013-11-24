package com.anod.car.home.model;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

/**
 * Cache of application icons. Icons can be made from any thread.
 */
public class AppsListCache {
	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
	private final ArrayList<CacheEntry> mCache = new ArrayList<CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);

	private final CarWidgetApplication mContext;
	private final PackageManager mPackageManager;

	public static class CacheEntry {
		public ComponentName componentName;
		public Bitmap icon;
		public String title;
		public String activityName;
	}


	public AppsListCache(CarWidgetApplication context) {
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
			/*
			 * entry.icon = UtilitiesBitmap.createIconBitmap(
			 * info.activityInfo.loadIcon(mPackageManager), mContext );
			 */
		}
	}

	public void sort() {
		Collections.sort(mCache, new DisplayNameComparator());
	}

	public static class DisplayNameComparator implements Comparator<CacheEntry> {
		private final Collator sCollator = Collator.getInstance();

		public final int compare(CacheEntry a, CacheEntry b) {
			return sCollator.compare(a.title, b.title);
		}

	}

	public ArrayList<CacheEntry> getCacheEntries() {
		synchronized (mCache) {
			return mCache;
		}
	}

	public Bitmap fetchIcon(CacheEntry entry) {
		if (entry.icon != null) {
			return entry.icon;
		}

		Drawable d;
		try {
			d = mPackageManager.getActivityIcon(entry.componentName);
			entry.icon = UtilitiesBitmap.createSystemIconBitmap(d, mContext);
		} catch (NameNotFoundException e) {
			Utils.logd(e.getMessage());
		}
		return entry.icon;
	}

	public void fetchDrawableOnThread(final CacheEntry entry, final ImageView imageView) {
		if (entry.icon != null) {
			imageView.setImageBitmap(entry.icon);
			imageView.setVisibility(View.VISIBLE);
			return;
		}
		if (entry.componentName == null) {
			imageView.setVisibility(View.INVISIBLE);
			return;
		}

		final Handler handler = new ImageViewHandler(imageView);

		Thread thread = new Thread() {
			@Override
			public void run() {
				Bitmap bitmap = fetchIcon(entry);
				Message message = handler.obtainMessage(1, bitmap);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}

	private static class ImageViewHandler extends Handler {
		private final ImageView mImageView;
		
		public ImageViewHandler(ImageView imageView) {
			super();
			mImageView = imageView;
		}

		@Override
		public void handleMessage(Message message) {
			Bitmap icon = (Bitmap) message.obj;
			if (icon != null) {
				mImageView.setImageBitmap(icon);
			}
		}
	}
}