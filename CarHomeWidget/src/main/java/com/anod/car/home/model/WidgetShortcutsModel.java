package com.anod.car.home.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.anod.car.home.prefs.preferences.WidgetStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.IntentUtils;

import java.util.ArrayList;

public class WidgetShortcutsModel extends AbstractShortcutsContainerModel {

    private final Context mContext;

    private final int mAppWidgetId;

    private int mCount;

    public WidgetShortcutsModel(Context context, int appWidgetId) {
        super(context);
        mContext = context;
        mAppWidgetId = appWidgetId;
    }

    public void loadCount() {
        mCount = WidgetStorage.getLaunchComponentNumber(mContext, mAppWidgetId);
    }

    @Override
    public void updateCount(Integer count) {
        mCount = count;
        WidgetStorage.saveLaunchComponentNumber(count, mContext, mAppWidgetId);
    }

    @Override
    protected ArrayList<Long> loadShortcutIds() {
        loadCount();
        return WidgetStorage.getLauncherComponents(mContext, mAppWidgetId, mCount);
    }

    @Override
    protected void saveShortcutId(int position, long shortcutId) {
        WidgetStorage.saveShortcut(mContext, shortcutId, position, mAppWidgetId);
    }

    @Override
    protected void dropShortcutId(int position) {
        WidgetStorage.dropShortcutPreference(position, mAppWidgetId, mContext);
    }

    @Override
    public int getCount() {
        return mCount;
    }


    @Override
    public void createDefaultShortcuts() {
        init();
        initShortcuts(mAppWidgetId);
    }

    public void initShortcuts(int appWidgetId) {
        ComponentName[] list = {
                new ComponentName("com.google.android.apps.maps",
                        "com.google.android.maps.driveabout.app.DestinationActivity"),
                new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity"),
                new ComponentName("com.android.htccontacts",
                        "com.android.htccontacts.DialerTabActivity"), //HTC Phone
                //
                new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity"),
                new ComponentName("com.htc.music", "com.htc.music.HtcMusic"),
                new ComponentName("com.sec.android.app.music",
                        "com.sec.android.app.music.MusicActionTabActivity"),
                //new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"),
                //new ComponentName("com.android.htccontacts", "com.android.htccontacts.BrowseLayerCarouselActivity"),
                new ComponentName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity"),
                new ComponentName("com.google.android.googlequicksearchbox",
                        "com.google.android.googlequicksearchbox.VoiceSearchActivity"),
        };

        int cellId = 0;
        Intent data = new Intent();
        ShortcutInfo info = null;
        for (int i = 0; i < list.length; i++) {
            data.setComponent(list[i]);
            if (!IntentUtils.isIntentAvailable(mContext, data)) {
                continue;
            }
            AppLog.d("Init shortcut - " + info + " Widget - " + appWidgetId);
            info = ShortcutInfoUtils.infoFromApplicationIntent(mContext, data);
            saveShortcut(cellId, info);
            cellId++;
            if (cellId == 5) {
                break;
            }
        }

    }

}
