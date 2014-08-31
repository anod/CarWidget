package com.anod.car.home.prefs;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetViewBuilder;
import com.anod.car.home.prefs.LookAndFeelActivity.SkinRefreshListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SkinPreviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<View>, SkinRefreshListener {
	
	private static final String ARG_POSITION = "position";
	private int mPosition;
	private LookAndFeelActivity mActivity;
	@InjectView(R.id.container_preview) ViewGroup mContainer;

	public static SkinPreviewFragment newInstance(int position) {
		SkinPreviewFragment f = new SkinPreviewFragment();
		
		Bundle args = new Bundle();
		args.putInt(ARG_POSITION, position);

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
		mActivity.onPreviewStart(mPosition);	
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mPosition = getArguments().getInt(ARG_POSITION);
		mActivity = (LookAndFeelActivity)activity;
		mActivity.onFragmentAttach(this, mPosition);
	}

	@Override
	public void onDetach() {
		mActivity.onFragmentDetach(mPosition);
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.skin_item, container, false);
        ButterKnife.inject(this,view);
		return view;
	}
	
	@Override
	public void refresh() {
		getLoaderManager().initLoader(0, null, this).forceLoad();
	}


	public static class ViewLoader extends AsyncTaskLoader<View> {
		private final LookAndFeelActivity mActivity;
		private final int mPosition;
		
		public ViewLoader(LookAndFeelActivity activity, int position) {
			super(activity);
			mActivity = activity;
			mPosition = position;
		}

		@Override
		public View loadInBackground() {
			WidgetViewBuilder builder = mActivity.createBuilder();
			builder.init();

			builder.setOverrideSkin(mActivity.getSkinItem(mPosition).value);
			RemoteViews rv = builder.build();

			return rv.apply( mActivity, null );
		}

	}
	
	@Override
	public Loader<View> onCreateLoader(int id, Bundle args) {
		return new ViewLoader(mActivity, mPosition);
	}

	@Override
	public void onLoadFinished(Loader<View> loader, View inflatedView) {
    	mActivity.onPreviewCreated(mPosition);
        if( mContainer.getChildCount() > 0 ) {
        	mContainer.removeAllViews();
        }
        // TODO
        if (inflatedView.getParent() != null) {
        	((ViewGroup)inflatedView.getParent()).removeView(inflatedView);
        }
        mContainer.addView( inflatedView );
		mContainer.invalidate();
        //mContainer.requestLayout();
	}

	@Override
	public void onLoaderReset(Loader<View> loader) {
		// TODO Auto-generated method stub
		
	}

}
