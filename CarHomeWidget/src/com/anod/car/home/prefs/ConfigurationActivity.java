package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

import com.anod.car.home.utils.TitleBarUtils;


abstract class ConfigurationActivity extends PreferenceActivity {
	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	protected Context mContext;
	private TitleBarUtils mTitleBarUtils;
	
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


}