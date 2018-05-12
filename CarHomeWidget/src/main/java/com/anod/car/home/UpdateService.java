package com.anod.car.home;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.anod.car.home.appwidget.Provider;
import com.anod.car.home.appwidget.ShortcutPendingIntent;
import com.anod.car.home.appwidget.WidgetViewBuilder;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.prefs.model.PrefsMigrate;
import com.anod.car.home.utils.Version;

public class UpdateService extends Service implements Runnable {

    private void performUpdate(Context context, int[] appWidgetIds) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        AppWidgetManager gm = AppWidgetManager.getInstance(context);
        int[] updateIds;
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            ComponentName thisWidget = Provider.getComponentName(context);
            updateIds = gm.getAppWidgetIds(thisWidget);
        } else {
            updateIds = appWidgetIds;
        }
        final int N = updateIds.length;

        Version version = new Version(context);

        registerBroadcastService(context, version.isProOrTrial());
        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        WidgetViewBuilder builder = new WidgetViewBuilder(context, new ShortcutPendingIntent(context));
        for (int i = 0; i < N; i++) {
            int appWidgetId = updateIds[i];

            PrefsMigrate.migrate(context, appWidgetId);

            builder.setAppWidgetId(appWidgetId);
            RemoteViews views = builder.init().build();
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    private void registerBroadcastService(Context context, boolean isProOrTrial) {
        boolean inCarEnabled = (isProOrTrial) ? InCarStorage.load(context).isInCarEnabled()
                : false;
        if (inCarEnabled) {
            BroadcastService.Companion.startService(context);
        } else {
            BroadcastService.Companion.stopService(context);
        }
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
        return null;
    }

}
