package com.anod.car.home;

import android.annotation.TargetApi;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.anod.car.home.appwidget.ShortcutPendingIntent;
import com.anod.car.home.appwidget.WidgetViewBuilder;
import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.utils.Utils;
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
        WidgetViewBuilder builder = new WidgetViewBuilder(context);
        builder.setPendingIntentHelper(new ShortcutPendingIntent(context));
        for (int i = 0; i < N; i++) {
            int appWidgetId = updateIds[i];

            if (Utils.IS_JELLYBEAN_OR_GREATER) {
                setKeyguardSettings(appWidgetManager, builder, appWidgetId);
            }

            RemoteViews views = builder
                    .setAppWidgetId(appWidgetId)
                    .init()
                    .build();
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setKeyguardSettings(AppWidgetManager appWidgetManager, WidgetViewBuilder builder,
            int appWidgetId) {
        Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
        int maxHeight = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, -1);
        boolean isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
        builder.setIsKeyguard(isKeyguard);
        builder.setWidgetHeightDp(maxHeight);
    }

    private void registerBroadcastService(Context context, boolean isProOrTrial) {
        boolean inCarEnabled = (isProOrTrial) ? InCarStorage.load(context).isInCarEnabled()
                : false;
        if (inCarEnabled) {
            BroadcastService.startService(context);
        } else {
            BroadcastService.stopService(context);
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
