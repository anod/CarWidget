package com.anod.car.home.appscache;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.model.AppsListCache.CacheEntry;
import com.anod.car.home.utils.UtilitiesBitmap;

public class AppsCacheAdapter extends ArrayAdapter<CacheEntry> {
	private AppsListCache mAppsListCache;
	private Bitmap mDefaultIcon;

	private int mResource;
	private Context mContext; 
	
	public AppsCacheAdapter(Context context, int resource, List<CacheEntry> items, AppsListCache appsListCache) {
		super(context, resource, items);
		mResource = resource;
		mContext = context;
		mAppsListCache = appsListCache;
        mDefaultIcon = UtilitiesBitmap.makeDefaultIcon(context.getPackageManager());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mResource, null);
		}
		CacheEntry entry = getItem(position);

		TextView title = (TextView) v.findViewById(android.R.id.text1);
        ImageView icon = (ImageView) v.findViewById(R.id.app_icon);
        title.setText(entry.title);
        if (entry.icon == null) {
        	icon.setImageBitmap(mDefaultIcon);
        	mAppsListCache.fetchDrawableOnThread(entry, icon);
        } else {
        	icon.setImageBitmap(entry.icon);
        }
        v.setId(position);
        return v;
	}
	
}
