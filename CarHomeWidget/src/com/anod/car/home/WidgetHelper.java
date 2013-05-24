package com.anod.car.home;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetHelper {

	/**
	 * Build {@link ComponentName} describing this specific
	 * {@link android.appwidget.AppWidgetProvider}
	 */
	public static int[] getAllWidgetIds(Context context) {
		AppWidgetManager wm = AppWidgetManager.getInstance(context);
		List<ComponentName> providers = getWidgetProviders();

		new ComponentName(PACKAGE_THIS_APPWIDGET, PROVIDER_DEFAULT);

		int[] appWidgetIds = new int[0];
		for(ComponentName provider : providers) {
			int[] ids = wm.getAppWidgetIds(provider);
			appWidgetIds = concat(appWidgetIds,ids);
		}
		return appWidgetIds;
	}

	private static int[] concat(int[] A, int[] B) {
		int[] C= new int[A.length+B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);

		return C;
	}
}
