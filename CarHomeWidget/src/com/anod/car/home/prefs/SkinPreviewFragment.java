package com.anod.car.home.prefs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.anod.car.home.LauncherViewBuilder;
import com.anod.car.home.R;
import com.anod.car.home.prefs.SkinPreviewActivity.SkinRefreshListener;

public class SkinPreviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<View>, SkinRefreshListener {
	
	private static final String ARG_POSITION = "position";
	private int mPosition;
	private SkinPreviewActivity mActivity;
	private ViewGroup mContainer;

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
		mActivity = (SkinPreviewActivity)activity;
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
		mContainer = (ViewGroup)view.findViewById(R.id.container_preview);
		return view;
	}
	
	@Override
	public void refresh() {
		getLoaderManager().initLoader(0, null, this).forceLoad();
	}


	public static class ViewLoader extends	AsyncTaskLoader<View> {
		private SkinPreviewActivity mActivity;
		private int mPosition;
		
		public ViewLoader(SkinPreviewActivity activity, int position) {
			super(activity);
			mActivity = activity;
			mPosition = position;
		}

		@Override
		public View loadInBackground() {
			LauncherViewBuilder builder = mActivity.getBuilder();
			
			builder.setOverrideSkin(mActivity.getSkinItem(mPosition).value);
			RemoteViews rv = builder.build();

	        View inflatedView =  rv.apply( mActivity, null );
	        return inflatedView;
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
        mContainer.requestLayout();
	}

	@Override
	public void onLoaderReset(Loader<View> loader) {
		// TODO Auto-generated method stub
		
	}

}
