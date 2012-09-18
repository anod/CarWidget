package com.anod.car.home;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.anod.car.home.prefs.Configuration;
import com.anod.car.home.prefs.PickShortcutUtils;

public class ShortcutPendingIntent {

	
    public static final String INTENT_ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
	/**
     * Create an Intent to launch Configuration
     * @param appWidgetId
     * @param context
     * @return
     */
    public static PendingIntent getSettingsPendingInent(int appWidgetId, Context context, int cellId) {
    	Intent intent = new Intent(context, Configuration.class);
    	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    	if (cellId != PickShortcutUtils.INVALID_CELL_ID) {
    		intent.putExtra(PickShortcutUtils.EXTRA_CELL_ID, cellId);
    	}
    	String path = String.valueOf(appWidgetId) + " - " + String.valueOf(cellId);
    	Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),path);
    	intent.setData(data);
    	intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
    	return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /**
     * 
     * @param componentName
     * @param appWidgetId
     * @param context
     * @return
     */
    public static PendingIntent getShortcutPendingInent(Intent intent,int appWidgetId, Context context, int cellId) {
    	String action = intent.getAction();
    	boolean isCallPrivileged = (action != null && action.equals(INTENT_ACTION_CALL_PRIVILEGED));
    	if (intent.getExtras() == null && !isCallPrivileged) { // Samsung s3 bug
    		return PendingIntent.getActivity(context, 0 /* no requestCode */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	}
    	Intent launchIntent = new Intent(context, ShortcutActivity.class);
    	String path = String.valueOf(appWidgetId) + " - " + String.valueOf(cellId);
    	Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),path);
    	launchIntent.setData(data);
    	launchIntent.setAction(Intent.ACTION_MAIN);
    	launchIntent.putExtra(ShortcutActivity.EXTRA_INTENT, intent);
		return PendingIntent.getActivity(context, 0 /* no requestCode */, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
