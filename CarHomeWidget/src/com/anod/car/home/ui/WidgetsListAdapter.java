package com.anod.car.home.ui;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;

import java.util.HashMap;
import java.util.Set;

/**
 * @author alex
 * @date 5/27/13
 */

public class WidgetsListAdapter extends ArrayAdapter<Integer> {

	private AppWidgetManager mAppWidgetManager;
	private HashMap<Integer,SparseArray<ShortcutInfo>> mWidgetShortcuts;

	private static int[] sIds = {
		R.id.imageView0,
		R.id.imageView1,
		R.id.imageView2,
		R.id.imageView3,
		R.id.imageView4,
		R.id.imageView5
	};

	public WidgetsListAdapter(Context context) {
		super(context, R.layout.settings_item);
		mAppWidgetManager = AppWidgetManager.getInstance(context);
		mWidgetShortcuts = new HashMap<Integer, SparseArray<ShortcutInfo>>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = null;
		if (convertView == null) {
			LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = li.inflate(R.layout.settings_item, null);
		} else {
			row = convertView;
		}

		int appWidgetId = getItem(position);
		SparseArray<ShortcutInfo> shortcuts = mWidgetShortcuts.get(appWidgetId);

		for( int i = 0; i < shortcuts.size(); i++) {
			ShortcutInfo info = shortcuts.get(i);
			ImageView view = (ImageView)row.findViewById(sIds[i]);
			if (info != null) {
				view.setImageBitmap(info.getIcon());
			} else {
				view.setVisibility(View.GONE);
			}
		}

		return row;
	}


	public void setWidgetShortcuts(HashMap<Integer, SparseArray<ShortcutInfo>> widgetShortcuts) {
		mWidgetShortcuts = widgetShortcuts;
		if (widgetShortcuts == null) {
			clear();
		} else {
			setData(widgetShortcuts.keySet());
		}
	}

	@TargetApi(11)
	public void setData(Set<Integer> data) {
		clear();
		if (data != null) {
			//If the platform supports it, use addAll, otherwise add in loop
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				addAll(data);
			}else{
				for(Integer item: data){
					add(item);
				}
			}
		}
	}
}