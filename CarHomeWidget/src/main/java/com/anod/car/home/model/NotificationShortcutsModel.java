package com.anod.car.home.model;

import android.content.Context;

import com.anod.car.home.prefs.preferences.InCarStorage;

import java.util.ArrayList;

public class NotificationShortcutsModel extends AbstractShortcutsContainerModel {

    private final Context mContext;

    public NotificationShortcutsModel(Context context) {
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


}
