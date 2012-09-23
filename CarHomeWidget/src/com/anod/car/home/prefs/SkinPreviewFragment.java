package com.anod.car.home.prefs;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.anod.car.home.LauncherViewBuilder;
import com.anod.car.home.R;

public class SkinPreviewFragment extends Fragment {
	
	private static final String ARG_POSITION = "position";
	private int mPosition;
	private SkinPreviewActivity mActivity;
	private LinearLayout mContainer;
	private ImageView mLoaderView;
	private LauncherViewBuilder mBuilder;
	private AsyncTask<Integer, Void, View> mTask;
	private int mAppWidgetId;

	public static SkinPreviewFragment newInstance(int position) {
		SkinPreviewFragment f = new SkinPreviewFragment();
		
		Bundle args = new Bundle();
		args.putInt(ARG_POSITION, position);

		f.setArguments(args);
		
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPosition = getArguments().getInt(ARG_POSITION);
		
		mLoaderView.setImageResource(mActivity.getSkinItem(mPosition).previewRes);

		mBuilder = new LauncherViewBuilder(mActivity);
		
		mTask = new CreateViewTask().execute(mAppWidgetId);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (SkinPreviewActivity)activity;
		mAppWidgetId = mActivity.getAppWidgetId();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.skin_item, container, false);
		mLoaderView = (ImageView)view.findViewById(R.id.theme_preview);
		mContainer = (LinearLayout)view.findViewById(R.id.container_preview);
		return view;
	}
	
	class CreateViewTask extends AsyncTask<Integer, Void, View> {
		
	    /* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {	}

		protected View doInBackground(Integer... appWidgetIds) {

	        int appWidgetId = appWidgetIds[0];
	        RemoteViews rv = mBuilder
	        	.setAppWidgetId(appWidgetId)
	        	.build();
	        
	        return rv.apply( mActivity, null );
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(View inflatedView) {
	    	mLoaderView.setVisibility(View.GONE);
	    	mLoaderView = null;
            if( mContainer.getChildCount() > 0 ) {
            	mContainer.removeAllViews();
            }
            mContainer.addView( inflatedView );
            mContainer.requestLayout();
        }
	}
}
