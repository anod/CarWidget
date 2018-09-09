package com.anod.car.home.incar;

import com.anod.car.home.R;
import com.anod.car.home.appwidget.ShortcutPendingIntent;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutIcon;
import com.anod.car.home.utils.Version;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

/**
 * @author alex
 * @date 2014-08-25
 */
public class ModeNotification {

    private static final int EXPIRED_ID = 2;

    private static final String PREFIX_NOTIF = "notif";

    private static final int[] NOTIF_BTN_IDS = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3};

    public static void showExpiredNotification(Context context) {
        Resources r = context.getResources();
        String notifText = r.getString(R.string.notif_consider);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_incar)
                .setAutoCancel(true)
                .setContentTitle(r.getString(R.string.notif_expired))
                .setTicker(notifText)
                .setContentTitle(notifText)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                .build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(EXPIRED_ID, notification);
        notificationManager.cancel(EXPIRED_ID);
    }

    public static Notification createNotification(Version version, Context context) {
        Intent notificationIntent = ModeService.Companion
                .createStartIntent(context, ModeService.MODE_SWITCH_OFF);
        Uri data = Uri.parse("com.anod.car.home.pro://mode/0/");
        notificationIntent.setData(data);

        Resources r = context.getResources();

        PendingIntent contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0);

        String text;
        if (version.isFree()) {
            text = context.getString(R.string.click_to_disable_trial, version.getTrialTimesLeft());
        } else {
            text = r.getString(R.string.click_to_disable);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_incar)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        NotificationShortcutsModel model = NotificationShortcutsModel.Companion.init(context);
        if (model.getFilledCount() > 0) {
            RemoteViews contentView = createShortcuts(context, model);
            contentView.setTextViewText(android.R.id.text1, text);
            notification.setContent(contentView);
        } else {
            notification.setContentTitle(r.getString(R.string.incar_mode_enabled));
            notification.setContentText(text);
        }

        return notification.build();
    }

    private static RemoteViews createShortcuts(Context context, NotificationShortcutsModel model) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);

        boolean viewGone = true;
        ShortcutPendingIntent spi = new ShortcutPendingIntent(context);
        for (int i = 0; i < model.getCount(); i++) {
            Shortcut info = model.get(i);
            int resId = NOTIF_BTN_IDS[i];
            if (info == null) {
                contentView.setViewVisibility(resId, (viewGone) ? View.GONE : View.INVISIBLE);
            } else {
                viewGone = false;
                ShortcutIcon icon = model.loadIcon(info.getId());

                contentView.setImageViewBitmap(resId, icon.bitmap);
                PendingIntent pendingIntent = spi.createShortcut(info.getIntent(), PREFIX_NOTIF, i);
                contentView.setOnClickPendingIntent(resId, pendingIntent);
            }
        }
        return contentView;
    }
}
