package com.anod.car.home.drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.ui.views.TwoLineButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author alex
 * @date 2014-10-21
 */
public class NavigationAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final NavigationList mItems;

    public boolean onClick(int position) {
        return mItems.onClick(mItems.get(position).id);
    }

    public static class ViewHolder {}

    public static class TitleViewHolder extends ViewHolder{
        @InjectView(android.R.id.title) TextView title;
        public TitleViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public void setTitle(NavigationList.Item item) {
            if (item.titleRes > 0) {
                title.setText(item.titleRes);
            } else if (item.titleText!= null) {
                title.setText(item.titleText);
            }
        }
    }
    public static class ActionViewHolder extends ViewHolder{
        @InjectView(R.id.action) TwoLineButton button;
        public ActionViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public void setAction(NavigationList.ActionItem action) {
            if (action.titleRes > 0) {
                button.setTitle(action.titleRes);
            } else if (action.titleText != null) {
                button.setTitle(action.titleText);
            }

            if (action.summaryRes > 0) {
                button.setSummary(action.summaryRes);
            } else if (action.summaryText != null)  {
                button.setSummary(action.summaryText);
            } else {
                button.setSummaryVisibility(View.GONE);
            }
            button.setIcon(action.iconRes);
        }
    }

    public NavigationAdapter(Context context, NavigationList items) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public NavigationList.Item getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        NavigationList.Item item = getItem(position);
        if (view == null) {
            int type = getItemViewType(item);
            view = mInflater.inflate(getItemViewResource(type), parent, false);
            holder = createViewHolder(type, view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        if (item instanceof NavigationList.ActionItem) {
            ((ActionViewHolder) holder).setAction((NavigationList.ActionItem) item);
        } else {
            ((TitleViewHolder) holder).setTitle(item);
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private int getItemViewResource(int type) {
        if (type == 0) {
            return R.layout.navigation_title;
        }
        return R.layout.navigation_action;
    }

    private ViewHolder createViewHolder(int type, View view) {
        if (type == 0) {
            return new TitleViewHolder(view);
        }
        return new ActionViewHolder(view);
    }


    @Override
    public int getItemViewType(int position) {
        return getItemViewType(mItems.get(position));
    }

    private int getItemViewType(NavigationList.Item item) {
        return item instanceof NavigationList.TitleItem ? 0 : 1;
    }
}
