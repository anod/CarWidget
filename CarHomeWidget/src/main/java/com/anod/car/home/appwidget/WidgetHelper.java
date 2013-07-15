package com.anod.car.home.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetHelper {
	public static final String PROVIDER_DEFAULT = "com.anod.car.home.LargeProvider";
	/**
	 * Build {@link ComponentName} describing this specific
	 * {@link android.appwidget.AppWidgetProvider}
	 */
	public static int[] getAllWidgetIds(Context context) {
		AppWidgetManager wm = AppWidgetManager.getInstance(context);
		ComponentName provider = new ComponentName(context.getPackageName(), PROVIDER_DEFAULT);

		int[] appWidgetIds = wm.getAppWidgetIds(provider);
		return appWidgetIds;
	}

}
