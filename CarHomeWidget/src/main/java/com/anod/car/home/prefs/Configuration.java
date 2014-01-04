package com.anod.car.home.prefs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.DebugActivity;
import com.anod.car.home.R;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.PickShortcutUtils.PreferenceKey;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.views.ShortcutPreference;

public class Configuration extends ConfigurationPreferenceFragment implements PreferenceKey, ShortcutPreference.DropCallback {
	public static final String DEBUG_ACTIVITY = "debug-activity";
	private ShortcutsModel mModel;
	private PickShortcutUtils mPickShortcutUtils;
	private static final String LOOK_AND_FEEL = "look-and-feel";
	private static final String INCAR = "incar";
	private static final String BACKUP = "backup";
	private PreferenceCategory mCategory;

	@Override
	protected int getXmlResource() {
		return R.xml.preferences;
	}
	
	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		mModel = new LauncherShortcutsModel(mContext, mAppWidgetId);
		mPickShortcutUtils = new PickShortcutUtils(this, mModel, this);
		mPickShortcutUtils.onRestoreInstanceState(savedInstanceState);
		mCategory = (PreferenceCategory)findPreference("shortcuts");

		if (BuildConfig.DEBUG) {
			setIntent(DEBUG_ACTIVITY, DebugActivity.class, 0);
		} else {
			((PreferenceCategory)findPreference("advanced-category")).removePreference(findPreference(DEBUG_ACTIVITY));
		}

		setIntent(LOOK_AND_FEEL, LookAndFeelActivity.class, mAppWidgetId);
		showFragmentOnClick(BACKUP, ConfigurationRestore.class);
		showFragmentOnClick(INCAR, ConfigurationInCar.class);

		int cellId = getActivity().getIntent().getExtras().getInt(PickShortcutUtils.EXTRA_CELL_ID, PickShortcutUtils.INVALID_CELL_ID);
		if (cellId != PickShortcutUtils.INVALID_CELL_ID) {
			mPickShortcutUtils.showActivityPicker(cellId);
		}
        
		
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshShortcuts();
	}

	@Override
	protected int getOptionsMenuResource() {

		return R.menu.configuration;
	}


	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.apply) {
			ConfigurationActivity act = (ConfigurationActivity)getActivity();
			act.beforeFinish();
			act.finish();
			return true;
		}
		if (item.getItemId() == R.id.menu_number) {
			createNumberPickerDialog().show();
		}
		return super.onOptionsItemSelected(item);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private AlertDialog createNumberPickerDialog() {
		final String[] nums = mContext.getResources().getStringArray(R.array.shortcut_numbers);

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View npView = inflater.inflate(R.layout.numberpicker, null);

		final NumberPicker numberPicker = (NumberPicker) npView.findViewById(R.id.numberPicker);
		numberPicker.setMinValue(0);
		numberPicker.setMaxValue(nums.length - 1);
		numberPicker.setDisplayedValues(nums);

		String countStr = String.valueOf(mModel.getCount());
		for(int i = 0; i < nums.length; i++)
		{
			if (countStr.equals(nums[i])) {
				numberPicker.setValue(i);
				break;
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(npView)
				 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					 @Override
					 public void onClick(DialogInterface dialogInterface, int i) {
						int value = numberPicker.getValue();
						mModel.updateCount(Integer.valueOf(nums[value]));
						refreshShortcuts();
					 }
				 })
				 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					 @Override
					 public void onClick(DialogInterface dialogInterface, int i) {
						 dialogInterface.dismiss();
					 }
				 })
				.setTitle("Number of shortcuts")
		;
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPickShortcutUtils.onSaveInstanceState(outState);
	}

	private void refreshShortcuts() {

		mModel.init();
		mCategory.removeAll();
		for (int i = 0; i < mModel.getCount(); i++) {
			ShortcutPreference p = new ShortcutPreference(mContext);
			mPickShortcutUtils.initLauncherPreference(i, p);
			mCategory.addPreference(p);
			p.setDropCallback(this);
			mPickShortcutUtils.refreshPreference(p);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mPickShortcutUtils.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public String getInitialKey(int position) {
		return PreferencesStorage.getLaunchComponentKey(position);
	}

	@Override
	public String getCompiledKey(int position) {
		return PreferencesStorage.getLaunchComponentName(position, mAppWidgetId);
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public int onScrollRequest(int top) {

		int lastVisiblePos = getListView().getLastVisiblePosition();
		int firstVisiblePos = getListView().getFirstVisiblePosition();
		int lastVisibleIdx = lastVisiblePos - firstVisiblePos;
		if (lastVisibleIdx > 0) {
			lastVisibleIdx -= 1;
		}
		Log.d("onScrollRequest", "Last visible position: " + lastVisiblePos);
		View child = getListView().getChildAt(lastVisibleIdx);
		if (child == null) {
			Log.d("onScrollRequest", "Child is null ");
			return 0;
		}
		int lastVisibleTop = (int) child.getTop();
		if (top >= lastVisibleTop) {
//			getListView().sc
		}
		Log.d("onScrollRequest", "Last visible Top: " + lastVisibleTop + " Drop Top: " + top);

		return 0;
	}

	@Override
	public boolean onDrop(int oldCellId, int newCellId) {
		if  (oldCellId == newCellId) {
			return false;
		}
		mModel.move(oldCellId,newCellId);
		refreshShortcuts();
		return true;
	}
}
