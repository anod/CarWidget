package com.anod.car.home.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;

/**
 * @author alex
 * @date 5/27/13
 */

public class WidgetsListAdapter extends ArrayAdapter<Integer> {

	private SparseArray<SparseArray<ShortcutInfo>> mWidgetShortcuts;

	private static int[] sIds = {
		R.id.imageView0,
		R.id.imageView1,
		R.id.imageView2,
		R.id.imageView3,
		R.id.imageView4,
		R.id.imageView5,
        R.id.imageView6,
        R.id.imageView7,
	};

	public WidgetsListAdapter(Context context) {
		super(context, R.layout.settings_item);
		mWidgetShortcuts = new SparseArray<SparseArray<ShortcutInfo>>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = null;
		if (convertView == null) {
			LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = li.inflate(R.layout.settings_item, parent, false);
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


	public void setWidgetShortcuts(SparseArray<SparseArray<ShortcutInfo>> widgetShortcuts) {
		mWidgetShortcuts = widgetShortcuts;
		clear();
		if (widgetShortcuts != null) {
			for(int i =0; i<widgetShortcuts.size(); i++) {
				add(widgetShortcuts.keyAt(i));
			}
		}
	}

}