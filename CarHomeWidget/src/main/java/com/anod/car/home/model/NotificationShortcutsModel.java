package com.anod.car.home.model;

import android.content.Context;

import com.anod.car.home.prefs.model.InCarStorage;

import java.util.ArrayList;

public class NotificationShortcutsModel extends AbstractShortcutsContainerModel {

    private final Context mContext;

    public static NotificationShortcutsModel init(Context context)
    {
        NotificationShortcutsModel model = new NotificationShortcutsModel(context);
        model.init();
        return model;
    }

    private NotificationShortcutsModel(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void createDefaultShortcuts() {
        // Nothing
    }

    @Override
    protected void loadCount() {
        //nothing
    }

    @Override
    public int getCount() {
        return InCarStorage.NOTIFICATION_COMPONENT_NUMBER;
    }

    @Override
    public void updateCount(Integer count) {
        // :( Exception
    }

    @Override
    protected void saveShortcutId(int position, long shortcutId) {
        InCarStorage.saveNotifShortcut(mContext, shortcutId, position);

    }

    @Override
    protected void dropShortcutId(int position) {
        InCarStorage.dropNotifShortcut(position, mContext);
    }

    @Override
    protected ArrayList<Long> loadShortcutIds() {
        return InCarStorage.getNotifComponents(mContext);
    }

    public int getFilledCount() {
        ArrayList<Long> ids = this.loadShortcutIds();
        int count = 0;
        for (int i = 0; i < InCarStorage.NOTIFICATION_COMPONENT_NUMBER; i++) {
            count += (ids.get(i) == ShortcutInfo.NO_ID) ? 0 : 1;
        }
        return count;
    }
}
