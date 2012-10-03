package com.anod.car.home.prefs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.anod.car.home.R;
import com.anod.car.home.actionbarcompat.ActionBarHelper;
import com.anod.car.home.utils.Utils;


abstract class ConfigurationActivity extends PreferenceActivity {
	final private ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);
	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	protected Context mContext;
	
	protected static final int DIALOG_WAIT = 1;

	abstract protected int getXmlResource();
	abstract protected void onCreateImpl(Bundle savedInstanceState);
	
	protected boolean isAppWidgetIdRequired() {
		return true;
	}
	
	@Override
	final protected void onCreate(Bundle savedInstanceState) {
        mActionBarHelper.onCreate(savedInstanceState);
		if (isAppWidgetIdRequired()) {
			mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getIntent());
		}
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(getXmlResource());

		if (isAppWidgetIdRequired()) {
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent defaultResultValue = new Intent();
				defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, defaultResultValue);
			} else {
				finish();
			}
		}
		
		mContext = (Context) this;
		
		onCreateImpl(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (isAppWidgetIdRequired()) {
			outState.putInt("appWidgetId", mAppWidgetId);
		}
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		mActionBarHelper.onPostCreate(savedInstanceState);
		super.onPostCreate(savedInstanceState);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.configuration, menu);
        
        boolean retValue = false;
        retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
        retValue |= super.onCreateOptionsMenu(menu);
        return retValue;
    }

	protected void setIntent(String key, Class<?> cls, int appWidgetId ) {
		Preference pref = (Preference) findPreference(key);
		Intent intent = new Intent(this, cls);
		if (appWidgetId > 0) {
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		}
		pref.setIntent(intent);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAIT:
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setCancelable(true);
			String message = getResources().getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		}
		return null;
	}
	
	public void showWaitDialog() {
		showDialog(DIALOG_WAIT);
	}
	
	public void dismissWaitDialog() {
		try {
			dismissDialog(DIALOG_WAIT);
		} catch (IllegalArgumentException e) {}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.apply) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}