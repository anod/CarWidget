package com.anod.car.home.ui;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author alex
 * @date 5/27/13
 */

class WidgetsListAdapter extends ArrayAdapter<WidgetsListAdapter.Item> {


    interface Item { }

    static class LargeItem implements Item
    {
        SparseArray<ShortcutInfo> shortcuts;
        public int appWidgetId;

        LargeItem(int appWidgetId, SparseArray<ShortcutInfo> shortcuts)
        {
            this.appWidgetId = appWidgetId;
            this.shortcuts = shortcuts;
        }
    }

    static class ShortcutItem implements Item { }

    static class HintItem implements Item { }

    private final LayoutInflater mLayoutInflater;

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

    WidgetsListAdapter(Context context) {
        super(context, R.layout.widgets_item);
        mLayoutInflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (mCount == position) {
            return getHintView(view);
        }

        return getWidgetView(position, view, parent);
    }

    private View getHintView(View view) {
        if (view == null) {
            TextView textView = new TextView(getContext());
            textView.setText(R.string.configure_select_item_hint);
            view = textView;
        }
        return view;
    }

    private View getWidgetView(int position, View view, ViewGroup parent) {
        Item item = getItem(position);

        if (view == null) {
            if (item instanceof ShortcutItem) {
                view = new View(parent.getContext());
            } else if (item instanceof HintItem) {
                view = mLayoutInflater.inflate(R.layout.widgets_hint, parent, false);
            }
            else {
                view = mLayoutInflater.inflate(R.layout.widgets_item, parent, false);
            }
        }

        if (item instanceof LargeItem) {
            SparseArray<ShortcutInfo> shortcuts = ((LargeItem) item).shortcuts;
            int size = shortcuts.size();
            for (int i = 0; i < sIds.length; i++) {
                ImageView icon = (ImageView) view.findViewById(sIds[i]);
                ShortcutInfo info = null;
                if (i < size) {
                    info = shortcuts.get(i);
                }
                if (info != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageBitmap(info.getIcon());
                } else {
                    icon.setVisibility(View.INVISIBLE);
                }
            }
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
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

    void setResult(WidgetsListLoader.Result result) {
        clear();
        if (result == null) {
            mCount = 0;
            return;
        }
        mCount = result.large.size();
        if (result.shortcuts.length > 0) {
            add(new ShortcutItem());
            mCount++;
        }
        for (int i = 0; i < result.large.size(); i++) {
            add(new LargeItem(result.large.keyAt(i), result.large.valueAt(i)));
        }
        if (result.large.size() > 0) {
            add(new HintItem());
        }
    }

}