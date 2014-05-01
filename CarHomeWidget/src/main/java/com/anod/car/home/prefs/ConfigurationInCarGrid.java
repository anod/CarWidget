package com.anod.car.home.prefs;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import com.anod.car.home.R;
import com.anod.car.home.prefs.detection.Collection;
import com.anod.car.home.prefs.detection.Detection;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

import java.util.List;

/**
 * @author alex
 * @date 1/15/14
 */
public class ConfigurationInCarGrid extends Fragment {

	private GridLayout mDetectionsGrid;
	private GridLayout mActionsGrid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.incar_settings, container, false);

		mDetectionsGrid = (GridLayout) view.findViewById(R.id.grid_detections);
		mActionsGrid = (GridLayout) view.findViewById(R.id.grid_actions);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		InCar incar = PreferencesStorage.loadInCar(getActivity());

		Resources r = getResources();
		int height = (int) r.getDimension(R.dimen.incar_item_height);
		int width = (int) r.getDimension(R.dimen.incar_item_width);
		int margin = (int) r.getDimension(R.dimen.incar_item_margin);

		LayoutInflater inflater = getLayoutInflater(savedInstanceState);

		initDetections(incar, r, height, width, margin, inflater);

	}

	private void initDetections(InCar incar, Resources r, int height, int width, int margin, LayoutInflater inflater) {
		Collection detections = new Collection(incar);
		List<Detection> active = detections.getActive();
		for (Detection detection : active) {
			Button button = (Button) inflater.inflate(R.layout.incar_settings_item, null, false);

			setupButton(r, height, width, margin, detection.getIconRes(), detection.getShortTitleRes(), button);
			mDetectionsGrid.addView(button);
		}

		Button addButton = (Button) inflater.inflate(R.layout.incar_settings_item, null, false);
		setupButton(r, height, width, margin, R.drawable.ic_add_shortcut_holo, R.string.add, addButton);
		mDetectionsGrid.addView(addButton);
	}

	private void setupButton(Resources r, int height, int width, int margin, int iconRes, int titleRes, Button button) {
		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
		layoutParams.width = width;
		layoutParams.height = height;
		layoutParams.setGravity(Gravity.TOP | Gravity.LEFT);
		layoutParams.setMargins(0,0,margin,margin);
		button.setLayoutParams(layoutParams);
		Drawable icon = r.getDrawable(iconRes);
		button.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
		button.setText(titleRes);
	}
}
