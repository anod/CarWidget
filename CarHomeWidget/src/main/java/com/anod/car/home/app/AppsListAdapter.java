package com.anod.car.home.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.utils.AppIconUtils;
import com.anod.car.home.utils.UtilitiesBitmap;

import java.util.ArrayList;
import java.util.List;

public class AppsListAdapter extends ArrayAdapter<AppsList.Entry> {
	final private Bitmap mDefaultIcon;

	final private int mResource;
	final private Context mContext;
    final private AppIconUtils mAppIconUtils;

    public AppsListAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<AppsList.Entry>());
		mResource = resource;
		mContext = context;
		mAppIconUtils = new AppIconUtils(context);
        mDefaultIcon = UtilitiesBitmap.makeDefaultIcon(context.getPackageManager());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mResource, null);
		}
		AppsList.Entry entry = getItem(position);

		TextView title = (TextView) v.findViewById(android.R.id.text1);
        ImageView icon = (ImageView) v.findViewById(R.id.app_icon);
        title.setText(entry.title);
        if (entry.icon == null) {
        	icon.setImageBitmap(mDefaultIcon);
            mAppIconUtils.fetchDrawableOnThread(entry, icon);
        } else {
        	icon.setImageBitmap(entry.icon);
        }
        v.setId(position);
        return v;
	}
	
}
