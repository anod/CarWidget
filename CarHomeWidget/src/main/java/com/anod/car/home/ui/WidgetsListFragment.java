package com.anod.car.home.ui;

import android.content.Intent;
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
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.prefs.ConfigurationInCar;
import com.anod.car.home.utils.InCarStatus;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Version;

public class WidgetsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<SparseArray<SparseArray<ShortcutInfo>>> {
    private static final int INCAR_HEADER = 0;
    private WidgetsListAdapter mAdapter;

	private int[] mAppWidgetIds;
    private Version mVersion;
    private View mHeaderView;

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
        mAppWidgetIds = WidgetHelper.getAllWidgetIds(getActivity());
		getLoaderManager().initLoader(0, null, this).forceLoad();
        updateInCarHeader(mHeaderView);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new WidgetsListAdapter(getActivity());
        mVersion = new Version(getActivity());

        ListView lv = getListView();

        // Start out with a progress indicator.
        //setListShown(true);
        lv.setDivider(new ColorDrawable(Color.TRANSPARENT));
        lv.setDividerHeight(getResources().getDimensionPixelOffset(R.dimen.preference_item_margin));
		int padding = (int)getResources().getDimension(R.dimen.panel_header_margin);
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lv.getLayoutParams();
		mlp.setMargins(padding,padding,padding,0);

        mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.widgets_incar, null);
        lv.addHeaderView(mHeaderView);
        View footer = getActivity().getLayoutInflater().inflate(R.layout.widgets_hint, null);
        lv.addFooterView(footer);

        lv.getEmptyView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWizard(1);
            }
        });

        setListAdapter(mAdapter);
        // Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		//getLoaderManager().initLoader(0, null, this);

	}

    private void startWizard(int page) {
        Intent intent = new Intent(getActivity(), WizardActivity.class);
        intent.putExtra(WizardActivity.EXTRA_PAGE, page);
        startActivity(intent);
    }

    private void updateInCarHeader(final View view) {

        int status = InCarStatus.get(mAppWidgetIds.length, mVersion, getActivity());
        String active = getActivity().getString(InCarStatus.render(status));

        TextView incarTitleView = (TextView)view.findViewById(R.id.incarTitle);
        incarTitleView.setText(getString(R.string.pref_incar_mode_title) + " - " + active);

        TextView trialText = (TextView)view.findViewById(R.id.incarTrial);
        if (mVersion.isFreeAndTrialExpired()) {
            trialText.setText(getString(R.string.dialog_donate_title_expired) + " " + getString(R.string.notif_consider));
        } else if (mVersion.isFree()) {
            String activationsLeft = getResources().getQuantityString(R.plurals.notif_activations_left, mVersion.getTrialTimesLeft(), mVersion.getTrialTimesLeft());
            trialText.setText(getString(R.string.dialog_donate_title_trial) + " " + activationsLeft);
        } else {
            trialText.setVisibility(View.GONE);
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.widgets_list, container, false);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
        if (position == INCAR_HEADER) {
            int status = InCarStatus.get(mAppWidgetIds.length,mVersion,getActivity());
            if (status == InCarStatus.ENABLED) {
                if (mVersion.isFreeAndTrialExpired()) {
                    startActivity(IntentUtils.createProVersionIntent());
                } else if (mVersion.isFree()) {
                    startActivity(IntentUtils.createProVersionIntent());
                } else {
                    Intent intent = ConfigurationActivity.createFragmentIntent(getActivity(), ConfigurationInCar.class);
                    startActivity(intent);
                }
            } else {
                Intent intent = ConfigurationActivity.createFragmentIntent(getActivity(), ConfigurationInCar.class);
                startActivity(intent);
            }
            return;
        }
        if (position-1 < mAppWidgetIds.length) {
            Integer appWidgetId = (Integer) mAdapter.getItem(position-1);
            ((WidgetsListActivity) getActivity()).startConfigActivity(appWidgetId);
        }
	}

	@Override
	public Loader<SparseArray<SparseArray<ShortcutInfo>>> onCreateLoader(int i, Bundle bundle) {
		return new WidgetsListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<SparseArray<SparseArray<ShortcutInfo>>> loader, SparseArray<SparseArray<ShortcutInfo>> widgetShortcuts) {
		mAdapter.setWidgetShortcuts(widgetShortcuts);
/*
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
*/
	}

	@Override
	public void onLoaderReset(Loader<SparseArray<SparseArray<ShortcutInfo>>> loader) {
		mAdapter.setWidgetShortcuts(null);
	}

}