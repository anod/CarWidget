package com.anod.car.home;


import com.anod.car.home.incar.BroadcastService;
import com.anod.car.home.incar.ModeService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class Provider extends AppWidgetProvider {

	
    private static Provider sInstance;
    public static synchronized Provider getInstance() {
        if (sInstance == null) {
            sInstance = new Provider();
        }
        return sInstance;
    }
    
    public void performUpdate(Context context, int[] appWidgetIds) {
    	if (appWidgetIds == null) {
    		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    		ComponentName thisAppWidget = getComponentName(context); //TODO
    		appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
    	}
    	WidgetState.requestUpdate(appWidgetIds);
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
            Preferences.DropWidgetSettings(context, appWidgetIds);
    }
    
    @Override
	public void onEnabled(Context context) {
    	super.onEnabled(context);
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public void onDisabled(Context context) {
        Preferences.DropSettings(context);
    	// Launch over to service so it can perform update
    	final Intent updateIntent = new Intent(context, UpdateService.class);
    	context.stopService(updateIntent);
    	
    	if (BroadcastService.sRegistred == true) {
            final Intent recieverIntent = new Intent(context, BroadcastService.class);
            context.stopService(recieverIntent);
    	}    
    	
    	if (ModeService.sInCarMode == true) {
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
