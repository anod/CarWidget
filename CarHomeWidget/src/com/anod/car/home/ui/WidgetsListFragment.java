package com.anod.car.home.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.LookAndFeelActivity;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;

public class WidgetsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<HashMap<Integer,SparseArray<ShortcutInfo>>> {
	private WidgetsListAdapter mAdapter;

	private static final String ARG_WIDGET_IDS = "appWidgetIds";
	private int[] mAppWidgetIds;

	public static WidgetsListFragment newInstance(int[] appWidgetIds) {
		WidgetsListFragment f = new WidgetsListFragment();

		Bundle args = new Bundle();
		args.putIntArray(ARG_WIDGET_IDS, appWidgetIds);

		f.setArguments(args);

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

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		mAppWidgetIds = getArguments().getIntArray(ARG_WIDGET_IDS);
		mAdapter = new WidgetsListAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Integer appWidgetId = (Integer)mAdapter.getItem(position);
		((WidgetsListActivity)getActivity()).startConfigActivity(appWidgetId);
	}

	@Override
	public Loader<HashMap<Integer,SparseArray<ShortcutInfo>>> onCreateLoader(int i, Bundle bundle) {
		return new WidgetsListLoader(getActivity(), mAppWidgetIds);
	}

	@Override
	public void onLoadFinished(Loader<HashMap<Integer, SparseArray<ShortcutInfo>>> loader, HashMap<Integer, SparseArray<ShortcutInfo>> widgetShortcuts) {
		mAdapter.setWidgetShortcuts(widgetShortcuts);

		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<HashMap<Integer,SparseArray<ShortcutInfo>>> loader) {
		mAdapter.setWidgetShortcuts(null);
	}

}