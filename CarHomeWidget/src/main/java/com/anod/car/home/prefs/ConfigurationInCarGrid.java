package com.anod.car.home.prefs;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.prefs.action.Action;
import com.anod.car.home.prefs.detection.Collection;
import com.anod.car.home.prefs.detection.Detection;
import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.Utils;

/**
 * @author alex
 * @date 1/15/14
 */
public class ConfigurationInCarGrid extends Fragment {

	private GridLayout mDetectionsGrid;
	private GridLayout mActionsGrid;
	private int mActiveBackgroundColor;
	private int mInactiveBackgroundColor;
	private Drawable mActiveBackground;
	private Drawable mInactiveBackground;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.incar_settings, container, false);

		mDetectionsGrid = (GridLayout) view.findViewById(R.id.grid_detections);
		mActionsGrid = (GridLayout) view.findViewById(R.id.grid_actions);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.incar, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		InCar incar = PreferencesStorage.loadInCar(getActivity());

		getActivity().setTitle(R.string.incar_mode);

		Resources r = getResources();
		int height = (int) r.getDimension(R.dimen.incar_item_height);
		int width = (int) r.getDimension(R.dimen.incar_item_width);
		int margin = (int) r.getDimension(R.dimen.incar_item_margin);

		mActiveBackground = r.getDrawable(R.drawable.incar_item_bg_active_grey);
		mActiveBackgroundColor = r.getColor(R.color.panel_bg_normal);
		mInactiveBackground = r.getDrawable(R.drawable.incar_item_bg_grey);
		mInactiveBackgroundColor = r.getColor(R.color.panel_bg_pressed);


		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		initDetections(incar, r, height, width, margin, inflater);
		initActions(incar, r, height, width, margin, inflater);
	}

	private void initActions(InCar incar, Resources r, int height, int width, int margin, LayoutInflater inflater) {
		com.anod.car.home.prefs.action.Collection actions = new com.anod.car.home.prefs.action.Collection(incar);
		Action[] list = actions.getAll();
		for (Action action : list) {
			Button button = (Button) inflater.inflate(R.layout.incar_settings_item, null, false);

			setupButton(r, height, width, margin, action, button);
			mActionsGrid.addView(button);
		}

		final InCarItem addItem = new InCarItem() {
			@Override
			public boolean isActive() {
				return false;
			}

			@Override
			public int getIconRes() {
				return R.drawable.ic_add_shortcut_holo;
			}

			@Override
			public int getShortTitleRes() {
				return R.string.add;
			}

			@Override
			public int getSummaryRes() {
				return 0;
			}

			@Override
			public void onClick() {

			}
		};


		Button addButton = (Button) inflater.inflate(R.layout.incar_settings_item, null, false);
		setupButton(r, height, width, margin, addItem, addButton);
		mActionsGrid.addView(addButton);
	}

	private void initDetections(InCar incar, Resources r, int height, int width, int margin, LayoutInflater inflater) {
		Collection detections = new Collection(getActivity(), incar);
		Detection[] list = detections.getAll();
		for (Detection detection : list) {
			Button button = (Button) inflater.inflate(R.layout.incar_settings_item, null, false);

			setupButton(r, height, width, margin, detection, button);
			mDetectionsGrid.addView(button);
		}

	}

	private void setupButton(Resources r, int height, int width, int margin,final InCarItem item, Button button) {
		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
		layoutParams.width = width;
	//	layoutParams.height = height;
		layoutParams.setGravity(Gravity.TOP | Gravity.LEFT);
		layoutParams.setMargins(0,0,margin,margin);
		button.setLayoutParams(layoutParams);
		Drawable icon = r.getDrawable(item.getIconRes());
		button.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
		button.setText(item.getShortTitleRes());
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				item.onClick();
				if (item.isActive()) {
					setButtonBackground((Button) v, true);
				} else {
					setButtonBackground((Button) v, false);
				}
			}
		});
		button.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(getActivity(), item.getSummaryRes(), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		if (item.isActive()) {
			setButtonBackground(button, true);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setButtonBackground(Button button, boolean active) {
		if (Utils.IS_JELLYBEAN_OR_GREATER) {
			Drawable clone;
			if (active) {
				clone = mActiveBackground.getConstantState().newDrawable();
			} else {
				clone = mInactiveBackground.getConstantState().newDrawable();
			}
			button.setBackground(clone);
		} else {
			if (active) {
				button.setBackgroundColor(mActiveBackgroundColor);
			} else {
				button.setBackgroundColor(mInactiveBackgroundColor);
			}
		}
	}
}
