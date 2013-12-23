package com.anod.car.home.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.preference.PreferenceFragment;

import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.model.AppsListCache;


public class ConfigurationActivity extends CarWidgetActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback {
	private static final String BACK_STACK_PREFS = ":carwidget:prefs";

	public static Intent createFragmentIntent(Context context, Class<?> fragment) {
		Intent intent = new Intent(context, ConfigurationActivity.class);
		intent.putExtra(EXTRA_FRAGMENT, fragment);
		return intent;
	}

	public static final String EXTRA_FRAGMENT = "fragment";
	private int mAppWidgetId;

	public void setAppWidgetId(int appWidgetId) {
		mAppWidgetId = appWidgetId;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref_layout);
		if (savedInstanceState == null) {

			ConfigurationFragment conf = createFragmentInstance();

			conf.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, conf).commit();
		}
	}

	private ConfigurationFragment createFragmentInstance() {
		Intent intent = getIntent();
		if (intent == null) {
			return new Configuration();
		}
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return new Configuration();
		}
		Class fragmentClass = (Class)extras.get(EXTRA_FRAGMENT);
		if (fragmentClass == null) {
			return new Configuration();
		}
		String fragmentClassName = fragmentClass.getName();
		Bundle args = new Bundle();
		ConfigurationFragment conf = (ConfigurationFragment)Fragment.instantiate(this, fragmentClassName , args);
		return conf;
	}

	public void beforeFinish() {
		requestWidgetUpdate();
		cleanAppsCache();
	}

	@Override
	public void onBackPressed() {
		beforeFinish();
		super.onBackPressed();
	}
	
	private void requestWidgetUpdate() {
		if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(getIntent().getAction()) && mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			int[] appWidgetIds = new int[1];
			appWidgetIds[0] = mAppWidgetId;
			Provider appWidgetProvider = Provider.getInstance();
			appWidgetProvider.performUpdate(this, appWidgetIds);
		}
	}


	private void cleanAppsCache() {
		AppsListCache allAppsList = getApp().getAppListCache();
		if (allAppsList!=null) {
			allAppsList.flush();
		}
	}

	@Override
	public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
		//if (pref.getFragment() == null) {
		//	return false;
		//}
		//startPreferencePanel(pref.getFragment(), pref.getExtras(), pref.getTitleRes(), pref.getTitle(), null, 0);
		//return true;
		return false;
	}

	public void startPreferencePanel(String fragmentClass, Preference pref) {
		startPreferencePanel(fragmentClass, pref.getTitle(), null, 0);
	}

	public void startPreferencePanel(String fragmentClass, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
		Bundle args = new Bundle();
		Fragment f = Fragment.instantiate(this, fragmentClass, args);
		if (resultTo != null) {
			f.setTargetFragment(resultTo, resultRequestCode);
		}
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.main_fragment, f);
		if (titleText != null) {
			transaction.setBreadCrumbTitle(titleText);
		}
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.addToBackStack(BACK_STACK_PREFS);
		transaction.commitAllowingStateLoss();
	}
}