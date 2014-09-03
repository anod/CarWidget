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

import butterknife.ButterKnife;
import butterknife.InjectView;

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
	public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

		AppsList.Entry entry = getItem(position);

        holder.title.setText(entry.title);
        if (entry.icon == null) {
            holder.icon.setImageBitmap(mDefaultIcon);
            mAppIconUtils.fetchDrawableOnThread(entry, holder.icon);
        } else {
            holder.icon.setImageBitmap(entry.icon);
        }
        view.setId(position);
        return view;
	}

    static class ViewHolder {
        @InjectView(android.R.id.text1) TextView title;
        @InjectView(android.R.id.icon) ImageView icon;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
