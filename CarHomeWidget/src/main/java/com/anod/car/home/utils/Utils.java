package com.anod.car.home.utils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.R;


public class Utils {
	
	public static final String TAG = "CarHomeWidget";

	final public static boolean IS_HONEYCOMB_OR_GREATER = (Build.VERSION.SDK_INT >= 11);
	final public static boolean IS_ICS_OR_GREATER = (Build.VERSION.SDK_INT >= 14);
	final public static boolean IS_JELLYBEAN_OR_GREATER = (Build.VERSION.SDK_INT >= 16);

	/**
	 *
	 * @param context
	 * @return
	 */
	public static boolean isProInstalled(Context context) {
		try{
		    context.getPackageManager().getApplicationInfo("com.anod.car.home.pro", 0 );
		    return true;
		} catch( PackageManager.NameNotFoundException e ){
		    return false;
		}
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	public static boolean isFreeInstalled(Context context) {
		try{
			context.getPackageManager().getApplicationInfo("com.anod.car.home.free", 0 );
			return true;
		} catch( PackageManager.NameNotFoundException e ){
			return false;
		}
	}

	/**
	 *
	 * @param msg
	 */
	public static void logw(String msg) {
		Log.w(TAG, msg, new Throwable());
	}

	/**
	 *
	 * @param message
	 */
	public static void logd(String message) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, message);
		}
	}

	public static int readAppWidgetId(Bundle savedInstanceState, Intent launchIntent) {
		if (savedInstanceState != null) {
			return savedInstanceState.getInt("appWidgetId");
		} else {
			Bundle extras = launchIntent.getExtras();
			if (extras != null) {
				return extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			}
		}
		return AppWidgetManager.INVALID_APPWIDGET_ID;
	}
	
	public static float calcIconsScale(String scaleString) {
        return 1.0f+0.1f*Integer.valueOf(scaleString);
	}
	
    public static String componentToString(ComponentName component) {
    	return component.getPackageName() + "/" + component.getClassName();
    }
    
    public static ComponentName stringToComponent(String compString) {
        String[] compParts = compString.split("/");
        return new ComponentName(compParts[0],compParts[1]);
    }
    
	public static void startActivityForResultSafetly(Intent intent, int requestCode, Activity activity) {
		try
		{
			activity.startActivityForResult(intent, requestCode);
		}
		catch (ActivityNotFoundException activityNotFoundException)
		{
			Toast.makeText(activity, R.string.photo_picker_not_found, Toast.LENGTH_LONG).show();
		}
		catch (Exception exception)
		{
			String errStr = String.format(activity.getResources().getString(R.string.error_text), exception.getMessage());
			Toast.makeText(activity, errStr, Toast.LENGTH_LONG).show();
		}
	}
	
    public static void startActivitySafely(Intent intent, Context context) {
        try {
        	context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(context, context.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Widget does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }
    

	public static void saveAppWidgetId(Bundle outState, int mAppWidgetId) {
		outState.putInt("appWidgetId", mAppWidgetId);
	}
	
}
