package com.anod.car.home.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;

/**
 * @author alex
 * @date 5/27/13
 */

public class WidgetsListAdapter extends ArrayAdapter<Integer> {

    private final LayoutInflater mLayoutInflater;
    private SparseArray<SparseArray<ShortcutInfo>> mWidgetShortcuts;
    private int mCount;

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
		super(context, R.layout.widgets_item);
        mLayoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

	@Override
	public View getView(int position, View view, ViewGroup parent) {

        if (mCount == position) {
            return getHintView(view, parent);
        }

        return getWidgetView(position, view, parent);
	}

    private View getHintView(View view, ViewGroup parent) {
        if (view == null) {
            TextView textView = new TextView(getContext());
            textView.setText("Select an item to configure the widget");
            view = textView;
        }
        return view;
    }

    private View getWidgetView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.widgets_item, parent, false);
        }

        int appWidgetId = getItem(position);
        SparseArray<ShortcutInfo> shortcuts = mWidgetShortcuts.get(appWidgetId);

        int size = shortcuts.size();
        for( int i = 0; i < sIds.length; i++) {
            ImageView icon = (ImageView)view.findViewById(sIds[i]);
            ShortcutInfo info = null;
            if (i < size) {
                info = shortcuts.get(i);
            }
            if (info != null) {
                icon.setImageBitmap(info.getIcon());
            } else {
                icon.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mCount == 0) {
            return 0;
        }
        return (1 == position) ? 1 : 0;
    }

    @Override
    public int getCount() {
        return super.getCount();// + 1;
    }

    public void setWidgetShortcuts(SparseArray<SparseArray<ShortcutInfo>> widgetShortcuts) {
        mWidgetShortcuts = widgetShortcuts;
        mCount = (widgetShortcuts == null) ? 0 : widgetShortcuts.size();
		clear();
		if (widgetShortcuts != null) {
			for(int i =0; i<mCount; i++) {
				add(widgetShortcuts.keyAt(i));
			}
		}
	}

}