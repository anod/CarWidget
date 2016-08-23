package com.anod.car.home.model;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import java.util.ArrayList;

public abstract class AbstractShortcutsContainerModel implements ShortcutsContainerModel {

    private SparseArray<Shortcut> mShortcuts;

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
        mShortcuts = new SparseArray<>(getCount());
        ArrayList<Long> currentShortcutIds = loadShortcutIds();
        for (int cellId = 0; cellId < getCount(); cellId++) {
            long shortcutId = currentShortcutIds.get(cellId);
            Shortcut info = null;
            if (shortcutId != Shortcut.NO_ID) {
                info = mModel.loadShortcut(shortcutId);
            }
            mShortcuts.put(cellId, info);
        }
    }


    @Override
    public SparseArray<Shortcut> getShortcuts() {
        return mShortcuts;
    }

    @Override
    public Shortcut getShortcut(int position) {
        return mShortcuts.get(position);
    }

    @Override
    public void reloadShortcut(int position, long shortcutId) {
        if (shortcutId == Shortcut.NO_ID) {
            mShortcuts.put(position, null);
        } else {
            final Shortcut info = mModel.loadShortcut(shortcutId);
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
    public Shortcut saveShortcutIntent(int position, Intent data,
            boolean isApplicationShortcut) {
        final ShortcutInfoUtils.ShortcutWithIcon shortcut = ShortcutInfoUtils.createShortcut(mContext, data, isApplicationShortcut);
        saveShortcut(position, shortcut.info, shortcut.icon);
        return mShortcuts.get(position);
    }

    @Override
    public void saveShortcut(int position, Shortcut info, ShortcutIcon icon) {
        if (info == null) {
            mShortcuts.put(position, null);
        } else {
            long id = mModel.addItemToDatabase(mContext, info, icon);
            if (id == Shortcut.NO_ID) {
                mShortcuts.put(position, null);
            } else {
                Shortcut newInfo = new Shortcut(id, info);
                mShortcuts.put(position, newInfo);
                saveShortcutId(position, id);
            }
        }
    }

    @Override
    public void dropShortcut(int position) {
        Shortcut info = mShortcuts.get(position);
        if (info != null) {
            mModel.deleteItemFromDatabase(info.id);
            mShortcuts.put(position, null);
            dropShortcutId(position);
        }
    }

    public ShortcutModel getShortcutModel() {
        return mModel;
    }

    public ShortcutIcon loadIcon(long id)
    {
        return mModel.loadShortcutIcon(id);
    }
}
