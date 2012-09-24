package com.anod.car.home.prefs;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.anod.car.home.LauncherViewBuilder;
import com.anod.car.home.R;

public class SkinPreviewFragment extends Fragment {
	
	private static final String ARG_POSITION = "position";
	private int mPosition;
	private SkinPreviewActivity mActivity;
	private ViewGroup mContainer;
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
		mTask = new CreateViewTask().execute(mAppWidgetId);
		mActivity.onPreviewStart(mPosition);	
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
		mContainer = (ViewGroup)view.findViewById(R.id.container_preview);
		return view;
	}
	
	class CreateViewTask extends AsyncTask<Integer, Void, View> {
		
	    /* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {	}

		protected View doInBackground(Integer... appWidgetIds) {
			LauncherViewBuilder builder = mActivity.getBuilder();
			
			builder.setOverrideSkin(mActivity.getSkinItem(mPosition).value);
			RemoteViews rv = builder.build();

	        View inflatedView =  rv.apply( mActivity, null );
	        return inflatedView;
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(View inflatedView) {
	    	mActivity.onPreviewCreated(mPosition);
            if( mContainer.getChildCount() > 0 ) {
            	mContainer.removeAllViews();
            }
            mContainer.addView( inflatedView );
            mContainer.requestLayout();
        }
	}

}
