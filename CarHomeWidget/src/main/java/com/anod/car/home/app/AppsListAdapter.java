package com.anod.car.home.app;

import com.anod.car.home.model.AppsList;
import com.anod.car.home.utils.UtilitiesBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;

public class AppsListAdapter extends ArrayAdapter<AppsList.Entry> {
    final private int mResource;
    final private Context mContext;
    private final AppIconLoader mIconLoader;
    private final BitmapDrawable mDefaultIconDrawable;

    public AppsListAdapter(Context context, int resource, AppIconLoader iconLoader) {
        super(context, resource, new ArrayList<AppsList.Entry>());
        mResource = resource;
        mContext = context;
        Bitmap defaultIcon = UtilitiesBitmap.makeDefaultIcon(context.getPackageManager());
        mDefaultIconDrawable = new BitmapDrawable(context.getResources(), defaultIcon);
        mIconLoader = iconLoader;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        AppsList.Entry entry = getItem(position);

        holder.title.setText(entry.title);

        if (entry.componentName == null) {
            if (entry.iconRes > 0) {
                holder.icon.setVisibility(View.VISIBLE);
                mIconLoader.picasso()
                        .load(entry.iconRes)
                        .placeholder(mDefaultIconDrawable)
                        .into(holder.icon);
            } else {
                holder.icon.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.icon.setVisibility(View.VISIBLE);
            mIconLoader.picasso()
                    .load(Uri.fromParts(AppIconLoader.SCHEME,entry.componentName.flattenToShortString(),null))
                    .placeholder(mDefaultIconDrawable)
                    .into(holder.icon);
        }
        view.setId(position);
        return view;
    }


    static class ViewHolder {

        int position;

        @BindView(android.R.id.text1)
        TextView title;

        @BindView(android.R.id.icon)
        ImageView icon;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
