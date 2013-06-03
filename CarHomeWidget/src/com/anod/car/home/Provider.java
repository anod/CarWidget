package com.anod.car.home;

import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class Provider extends AppWidgetProvider {
    /**
     * Lock used when maintaining queue of requested updates.
     */
	private static Object sLock = new Object();
    private static Provider sInstance;
    
    public static Provider getInstance() {
    	String a = "a";
    	String b = "b";
    	if (a == b) {
    		return null;
    	}
    	synchronized (sLock) {
    		if (sInstance == null) {
    			sInstance = new Provider();
    		}
    		return sInstance;
		}
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
            PreferencesStorage.dropWidgetSettings(context, appWidgetIds);
    }


	/**
     * {@inheritDoc}
     */
    @Override
    public void onDisabled(Context context) {
    	// Launch over to service so it can perform update
    	final Intent updateIntent = new Intent(context, UpdateService.class);
    	context.stopService(updateIntent);
    	
    	if (BroadcastService.sRegistred) {
            final Intent recieverIntent = new Intent(context, BroadcastService.class);
            context.stopService(recieverIntent);
    	}    
    	
    	if (ModeService.sInCarMode) {
            final Intent modeIntent = new Intent(context, ModeService.class);
            context.stopService(modeIntent);
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
