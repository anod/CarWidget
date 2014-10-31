package com.anod.car.home.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.model.ShortcutInfo;

public class WidgetsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<SparseArray<SparseArray<ShortcutInfo>>> {
	private WidgetsListAdapter mAdapter;

	private int[] mAppWidgetIds;

	public static WidgetsListFragment newInstance() {
		WidgetsListFragment f = new WidgetsListFragment();

		return f;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAppWidgetIds = WidgetHelper.getAllWidgetIds(getActivity());
		mAdapter = new WidgetsListAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
        //setListShown(true);
        getListView().setDivider(new ColorDrawable(Color.TRANSPARENT));
        getListView().setDividerHeight(getResources().getDimensionPixelOffset(R.dimen.preference_item_margin));
		int padding = (int)getResources().getDimension(R.dimen.panel_header_margin);
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getListView().getLayoutParams();
		mlp.setMargins(padding,padding,padding,0);

        View view = getActivity().getLayoutInflater().inflate(R.layout.widgets_hint, null);
        getListView().addFooterView(view);

		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);

	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.widgets_list, container, false);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Integer appWidgetId = (Integer)mAdapter.getItem(position);
		((WidgetsListActivity)getActivity()).startConfigActivity(appWidgetId);
	}

	@Override
	public Loader<SparseArray<SparseArray<ShortcutInfo>>> onCreateLoader(int i, Bundle bundle) {
		return new WidgetsListLoader(getActivity(), mAppWidgetIds);
	}

	@Override
	public void onLoadFinished(Loader<SparseArray<SparseArray<ShortcutInfo>>> loader, SparseArray<SparseArray<ShortcutInfo>> widgetShortcuts) {
		mAdapter.setWidgetShortcuts(widgetShortcuts);

//		if (isResumed()) {
//			setListShown(true);
//		} else {
//			setListShownNoAnimation(true);
//		}
	}

	@Override
	public void onLoaderReset(Loader<SparseArray<SparseArray<ShortcutInfo>>> loader) {
		mAdapter.setWidgetShortcuts(null);
	}

}