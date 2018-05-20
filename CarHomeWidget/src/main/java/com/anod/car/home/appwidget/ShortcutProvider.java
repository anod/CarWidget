package com.anod.car.home.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.anod.car.home.R;
import com.anod.car.home.incar.SwitchInCarActivity;

/**
 * @author algavris
 * @date 28/07/2016.
 */
public class ShortcutProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_item_widget_shortcut);

        Intent activity = new Intent(context, SwitchInCarActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

        PendingIntent switchIntent = PendingIntent.getActivity(context, 0, activity, PendingIntent.FLAG_UPDATE_CURRENT);
//        pendingIntent.setData(Uri.parse("com.anod.car.home.pro://mode/switch"));
        views.setOnClickPendingIntent(R.id.incar, switchIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

}
