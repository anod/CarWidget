package com.anod.car.home.prefs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anod.car.home.R;

public class SkinPreviewFragment extends Fragment {
	
	private static final String ARG_POSITION = "position";
	private int mPosition;
	private ImageView mImageView;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.skin_item, container, false);
		mImageView = (ImageView)view.findViewById(R.id.theme_preview);
		mImageView.setImageResource(R.drawable.widget_preview);
		return view;
	}
	
	
}
