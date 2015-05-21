package com.anod.car.home.model;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import java.util.ArrayList;

public abstract class AbstractShortcutsContainerModel implements ShortcutsContainerModel {

    private SparseArray<ShortcutInfo> mShortcuts;

    private final Context mContext;

    private final ShortcutModel mModel;

    abstract protected void loadCount();

    abstract public int getCount();

    abstract protected void saveShortcutId(int position, long shortcutId);

    abstract protected void dropShortcutId(int position);

    abstract protected ArrayList<Long> loadShortcutIds();

    public AbstractShortcutsContainerModel(Context context) {
        mModel = new ShortcutModel(context);
        mContext = context;
    }

    @Override
    public void init() {
        loadCount();
        mShortcuts = new SparseArray<ShortcutInfo>(getCount());
        ArrayList<Long> currentShortcutIds = loadShortcutIds();
        for (int cellId = 0; cellId < getCount(); cellId++) {
            long shortcutId = currentShortcutIds.get(cellId);
            ShortcutInfo info = null;
            if (shortcutId != ShortcutInfo.NO_ID) {
                info = mModel.loadShortcut(shortcutId);
            }
            mShortcuts.put(cellId, info);
        }
    }


    @Override
    public SparseArray<ShortcutInfo> getShortcuts() {
        return mShortcuts;
    }

    @Override
    public ShortcutInfo getShortcut(int position) {
        return mShortcuts.get(position);
    }

    @Override
    public void reloadShortcut(int position, long shortcutId) {
        if (shortcutId == ShortcutInfo.NO_ID) {
            mShortcuts.put(position, null);
        } else {
            final ShortcutInfo info = mModel.loadShortcut(shortcutId);
            mShortcuts.put(position, info);
        }
    }

    public void move(int from, int to) {
        if (from == to) {
            return;
        }
        ArrayList<Long> currentShortcutIds = loadShortcutIds();
        long srcShortcutId = currentShortcutIds.get(from);
        long dstShortcutId = currentShortcutIds.get(to);

        saveShortcutId(from, dstShortcutId);
        saveShortcutId(to, srcShortcutId);

    }

    @Override
    public ShortcutInfo saveShortcutIntent(int position, Intent data,
            boolean isApplicationShortcut) {
        final ShortcutInfo info = ShortcutInfoUtils
                .createShortcut(mContext, data, position, isApplicationShortcut);
        saveShortcut(position, info);
        return mShortcuts.get(position);
    }

    @Override
    public void saveShortcut(int position, ShortcutInfo info) {
        mShortcuts.put(position, info);
        if (info != null) {
            mModel.addItemToDatabase(mContext, info, position);
            saveShortcutId(position, info.id);
        }
    }

    @Override
    public void dropShortcut(int position) {
        ShortcutInfo info = mShortcuts.get(position);
        if (info != null) {
            mModel.deleteItemFromDatabase(info.id);
            mShortcuts.put(position, null);
            dropShortcutId(position);
        }
    }

    public ShortcutModel getShortcutModel() {
        return mModel;
    }
}
