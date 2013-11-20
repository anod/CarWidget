package com.anod.car.home.prefs;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences;
import com.anod.car.home.utils.Utils;

/**
 * @author alex
 * @date 11/19/13
 */
abstract public class ConfigurationFragment extends PreferenceFragment {
	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	protected Context mContext;


	protected static final int DIALOG_WAIT = 1;

	abstract protected int getXmlResource();
	abstract protected void onCreateImpl(Bundle savedInstanceState);

	protected boolean isAppWidgetIdRequired() {
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		int res = getOptionsMenuResource();
		if (res == 0) {
			super.onCreateOptionsMenu(menu, inflater);
			return;
		}
		inflater.inflate(res, menu);
		super.onCreateOptionsMenu(menu, inflater);

	}

	protected Preference initWidgetPrefCheckBox(String name, boolean checked) {
		CheckBoxPreference pref = (CheckBoxPreference) initWidgetPref(name);
		pref.setChecked(checked);
		return pref;
	}

	@SuppressWarnings("deprecation")
	protected Preference initWidgetPref(String name) {
		Preference pref = (Preference) findPreference(name);
		String key = WidgetSharedPreferences.getName(name, mAppWidgetId);
		pref.setKey(key);
		return pref;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (isAppWidgetIdRequired()) {
			mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getActivity().getIntent());
		}
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(getXmlResource());

		if (isAppWidgetIdRequired()) {
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent defaultResultValue = new Intent();
				defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				getActivity().setResult(Activity.RESULT_OK, defaultResultValue);
			} else {
				Utils.logw("AppWidgetId required");
				getActivity().finish();
				return;
			}
		}

		mContext = (Context) getActivity();

		onCreateImpl(savedInstanceState);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (isAppWidgetIdRequired()) {
			outState.putInt("appWidgetId", mAppWidgetId);
		}
	}

	protected int getOptionsMenuResource() {
		return 0;
	}



	@SuppressWarnings("deprecation")
	protected void setIntent(String key, Class<?> cls, int appWidgetId ) {
		Preference pref = (Preference) findPreference(key);
		Intent intent = new Intent(mContext, cls);
		if (appWidgetId > 0) {
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		}
		pref.setIntent(intent);
	}

	@SuppressWarnings("deprecation")
	protected void setFragmentIntent(String key, Class<?> fragment, int appWidgetId ) {
		Preference pref = (Preference) findPreference(key);
		Intent intent = ConfigurationActivity.createFragmentIntent(mContext, fragment);
		if (appWidgetId > 0) {
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		}
		pref.setIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.apply) {
			getActivity().finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// TODO: save/restore
	private Dialog mCurrentDialog;


	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_WAIT) {
			ProgressDialog waitDialog = new ProgressDialog(mContext);
			waitDialog.setCancelable(true);
			String message = getResources().getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		}
		return null;
	}

	public void showDialog(int id) {
		mCurrentDialog = onCreateDialog(id);
		if (mCurrentDialog != null) {
			mCurrentDialog.show();
		}
	}

	public void dismissDialog(int id) {
		if (mCurrentDialog != null) {
			mCurrentDialog.dismiss();
		}
	}
}
