package com.anod.car.home.incar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.ShortcutPendingIntent;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

/**
 * @author alex
 * @date 2014-08-25
 */
public class ModeNotification {
    private static final int DEBUG_ID = 3;
    private static final int EXPIRED_ID = 2;

    private static final String PREFIX_NOTIF = "notif";
    private static final int[] NOTIF_BTN_IDS = { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3 };

    public static void showExpiredNotification(Context context) {
        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.ic_stat_incar;
        String notifTitle = context.getResources().getString(R.string.notif_expired);
        String notifText = context.getResources().getString(R.string.notif_consider);
        notification.tickerText = notifTitle;
        notification.setLatestEventInfo(context, notifTitle, notifText, PendingIntent.getActivity(context, 0, new Intent(), 0));

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(ns);
        notificationManager.notify(EXPIRED_ID, notification);
        notificationManager.cancel(EXPIRED_ID);
    }

    public static Notification createNotification(Version version, Context context) {
        Intent notificationIntent = ModeService.createStartIntent(context, ModeService.MODE_SWITCH_OFF);
        Uri data = Uri.parse("com.anod.car.home.pro://mode/0/");
        notificationIntent.setData(data);

        PendingIntent contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0);

        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.icon = R.drawable.ic_stat_incar;

        RemoteViews contentView = createShortcuts(context);
        if (version.isFree()) {
            contentView.setTextViewText(R.id.text, context.getString(R.string.click_to_disable_trial, version.getTrialTimesLeft()));
        }
        notification.contentIntent = contentIntent;
        notification.contentView = contentView;
        setNotificationPriority(notification);

        return notification;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setNotificationPriority(Notification notification) {
        if (Utils.IS_JELLYBEAN_OR_GREATER) {
            notification.priority = Notification.PRIORITY_MAX;
        }
    }

    private static RemoteViews createShortcuts(Context context) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        NotificationShortcutsModel model = new NotificationShortcutsModel(context);
        model.init();
        boolean viewGone = true;
        ShortcutPendingIntent spi = new ShortcutPendingIntent(context);
        for (int i = 0; i < model.getCount(); i++) {
            ShortcutInfo info = model.getShortcut(i);
            int resId = NOTIF_BTN_IDS[i];
            if (info == null) {
                contentView.setViewVisibility(resId, (viewGone) ? View.GONE : View.INVISIBLE);
            } else {
                viewGone = false;
                contentView.setImageViewBitmap(resId, info.getIcon());
                PendingIntent pendingIntent = spi.createShortcut(info.intent, PREFIX_NOTIF, i);
                contentView.setOnClickPendingIntent(resId, pendingIntent);
            }
        }
        return contentView;
    }
}
