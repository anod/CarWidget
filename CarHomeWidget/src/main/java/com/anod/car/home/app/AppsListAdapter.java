package com.anod.car.home.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.model.AppsList;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AppsListAdapter extends ArrayAdapter<AppsList.Entry> {
	final private Bitmap mDefaultIcon;

	final private int mResource;
	final private Context mContext;
    private final AppIconLoader mIconLoader;

    public AppsListAdapter(Context context, int resource, AppIconLoader iconLoader) {
		super(context, resource, new ArrayList<AppsList.Entry>());
		mResource = resource;
		mContext = context;
        mDefaultIcon = UtilitiesBitmap.makeDefaultIcon(context.getPackageManager());
        mIconLoader = iconLoader;
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

        if (entry.componentName == null) {
            if (entry.iconRes > 0) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(entry.iconRes);
                mIconLoader.releaseImageView(holder.icon);
            } else {
                holder.icon.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageBitmap(mDefaultIcon);
            mIconLoader.loadImage(entry.componentName.flattenToShortString(), holder.icon);

        }
        view.setId(position);
        return view;
	}


    static class ViewHolder {
        int position;
        @InjectView(android.R.id.text1) TextView title;
        @InjectView(android.R.id.icon) ImageView icon;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
