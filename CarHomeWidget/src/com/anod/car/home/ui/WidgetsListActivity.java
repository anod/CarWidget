package com.anod.car.home.ui;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.AsyncTaskLoader;
import android.view.View;
import android.widget.ListView;
import android.widget.RemoteViews;
import com.anod.car.home.WidgetHelper;
import com.anod.car.home.appwidget.LauncherViewBuilder;
import com.anod.car.home.prefs.LookAndFeelActivity;
import com.anod.car.home.prefs.PickShortcutUtils;
import com.anod.car.home.utils.IntentUtils;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetsListActivity extends FragmentActivity {
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int[] appWidgetIds = WidgetHelper.getAllWidgetIds(this);
		if (appWidgetIds.length == 1 && appWidgetIds[0] != AppWidgetManager.INVALID_APPWIDGET_ID) {
			startConfigActivity(appWidgetIds[0]);
			return;
		};

		mContext = this;

		// to give support on lower android version, we are not calling getFragmentManager()
		FragmentManager fm = getSupportFragmentManager();

		// Create the list fragment and add it as our sole content.
		if (fm.findFragmentById(android.R.id.content) == null) {
			WidgetsListFragment f = WidgetsListFragment.newInstance(appWidgetIds);
			fm.beginTransaction().add(android.R.id.content, f).commit();
		}
	}

	public void startConfigActivity(int appWidgetId) {
		Intent configIntent = IntentUtils.createSettingsIntent(this, appWidgetId, PickShortcutUtils.INVALID_CELL_ID);
		startActivity(configIntent);
		finish();
	}


}