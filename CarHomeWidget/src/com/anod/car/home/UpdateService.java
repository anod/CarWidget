package com.anod.car.home;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.anod.car.home.appwidget.LauncherViewBuilder;
import com.anod.car.home.appwidget.ShortcutPendingIntent;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.Version;

public class UpdateService extends Service implements Runnable {
	
	private void performUpdate(Context context, int[] appWidgetIds) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		AppWidgetManager gm = AppWidgetManager.getInstance(context);
		if (appWidgetIds == null || appWidgetIds.length == 0) {
			ComponentName thisWidget = Provider.getComponentName(context);
			appWidgetIds = gm.getAppWidgetIds(thisWidget);
		}
		final int N = appWidgetIds.length;

		Version version = new Version(context);
		
		registerBroadcastService(context, version.isProOrTrial());
		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		LauncherViewBuilder builder = new LauncherViewBuilder(context);
		builder.setPendingIntentHelper(new ShortcutPendingIntent(context));
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = builder
				.setAppWidgetId(appWidgetId)
				.init()
				.build();
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	private void registerBroadcastService(Context context, boolean isProOrTrial) {
		boolean inCarEnabled = (isProOrTrial) ? PreferencesStorage.isInCarModeEnabled(context) : false;
		if (inCarEnabled) {
			final Intent updateIntent = new Intent(context, BroadcastService.class);
			context.startService(updateIntent);
		} else {
			if (BroadcastService.sRegistred == true) {
				final Intent updateIntent = new Intent(context, BroadcastService.class);
				context.stopService(updateIntent);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Only start processing thread if not already running
		synchronized (WidgetState.sLock) {
			if (!WidgetState.sThreadRunning) {
				WidgetState.sThreadRunning = true;
				new Thread(this).start();
			}
		}
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void run() {
		while (true) {
			int[] appWidgetIds;
			synchronized (WidgetState.sLock) {
				if (!WidgetState.hasMoreUpdates()) {
					WidgetState.clearLocked();
					stopSelf();
					return;
				}
				appWidgetIds = WidgetState.collectAppWidgetIdsLocked();
			}
			performUpdate(this, appWidgetIds);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
