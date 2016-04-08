package com.anod.car.home;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.prefs.preferences.WidgetStorage;


public class Provider extends AppWidgetProvider {

    /**
     * Lock used when maintaining queue of requested updates.
     */
    private final static Object sLock = new Object();

    private static Provider sInstance;

    public static Provider getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new Provider();
            }
            return sInstance;
        }
    }

    public void requestUpdate(Context context, int appWidgetId) {
        int[] appWidgetIds = new int[1];
        appWidgetIds[0] = appWidgetId;
        performUpdate(context, appWidgetIds);
    }

    public void performUpdate(Context context, int[] appWidgetIds) {
        if (appWidgetIds == null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = getComponentName(context); //TODO
            int[] allAppWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            WidgetState.requestUpdate(allAppWidgetIds);
        } else {
            WidgetState.requestUpdate(appWidgetIds);
        }
        // Launch over to service so it can perform update
        final Intent updateIntent = new Intent(context, UpdateService.class);
        context.startService(updateIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        performUpdate(context, appWidgetIds);
    }

    /**
     * Will be executed when the widget is removed from the homescreen
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // Drop the settings if the widget is deleted
        WidgetStorage.dropWidgetSettings(context, appWidgetIds);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisabled(Context context) {
        // Launch over to service so it can perform update
        final Intent updateIntent = new Intent(context, UpdateService.class);
        context.stopService(updateIntent);

        BroadcastService.stopService(context);

        if (ModeService.sInCarMode) {
            final Intent modeIntent = ModeService
                    .createStartIntent(context, ModeService.MODE_SWITCH_OFF);
            context.stopService(modeIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, -1);

        Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
        boolean isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

        if (isKeyguard && maxHeight != -1) {
            Log.d("CarWidgetOptions", "isKeyguard: " + isKeyguard + ", maxHeight: " + maxHeight);
            performUpdate(context, new int[]{appWidgetId});
        }
    }

    /**
     * Build {@link ComponentName} describing this specific
     * {@link AppWidgetProvider}
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context, LargeProvider.class);
    }
}
