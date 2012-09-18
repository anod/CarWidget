package com.anod.car.home.prefs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Window;

import com.anod.car.home.R;
import com.anod.car.home.utils.TitleBarUtils;


abstract class ConfigurationActivity extends PreferenceActivity {
	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	protected Context mContext;
	private TitleBarUtils mTitleBarUtils;
	
	protected static final int DIALOG_WAIT = 1;

	abstract protected int getTitleResource();
	abstract protected int getXmlResource();
	abstract protected void onCreateImpl(Bundle savedInstanceState);
	
	protected boolean isAppWidgetIdRequired() {
		return true;
	}
	
	@Override
	final protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(getXmlResource());
		setTitle(getTitleResource());
		mTitleBarUtils = new TitleBarUtils(this);
		mTitleBarUtils.setCustomTitleBar();

		if (isAppWidgetIdRequired()) {
			Intent launchIntent = getIntent();
			Bundle extras = launchIntent.getExtras();
			if (extras != null) {
				mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	
				Intent defaultResultValue = new Intent();
				defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, defaultResultValue);
			} else {
				finish();
			}
		}
		
		mContext = (Context) this;
		mTitleBarUtils.setupActionBar();
		
		onCreateImpl(savedInstanceState);
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
}