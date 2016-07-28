package com.anod.car.home.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetHelper {

    static final String PROVIDER_DEFAULT = "com.anod.car.home.LargeProvider";
    private static final String PROVIDER_SHORTCUT = "com.anod.car.home.appwidget.ShortcutProvider";

    /**
     * Build {@link ComponentName} describing this specific
     * {@link android.appwidget.AppWidgetProvider}
     */
    public static int[] getLargeWidgetIds(Context context) {
        AppWidgetManager wm = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context.getPackageName(), PROVIDER_DEFAULT);

        int[] appWidgetIds = wm.getAppWidgetIds(provider);
        return appWidgetIds;
    }

    public static int[] getShortcutWidgetIds(Context context) {
        AppWidgetManager wm = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context.getPackageName(), PROVIDER_SHORTCUT);

        int[] appWidgetIds = wm.getAppWidgetIds(provider);
        return appWidgetIds;
    }

    public static int[] getAllWidgetIds(Context context) {
        int[] array1 = getLargeWidgetIds(context);
        int[] array2 = getShortcutWidgetIds(context);

        int[] total = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, total, 0, array1.length);
        System.arraycopy(array2, 0, total, array1.length, array2.length);

        return total;
    }

}
