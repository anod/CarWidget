package com.anod.car.home.model;

import android.content.Intent;
import android.util.SparseArray;

public interface ShortcutsContainerModel {

    void createDefaultShortcuts();

    void init();

    void reloadShortcut(int cellId, long shortcutId);

    SparseArray<Shortcut> getShortcuts();

    Shortcut getShortcut(int cellId);

    Shortcut saveShortcutIntent(int cellId, Intent data, boolean isApplicationShortcut);

    void saveShortcut(int cellId, Shortcut info, ShortcutIcon icon);

    void dropShortcut(int cellId);

    void move(int from, int to);

    int getCount();

    void updateCount(Integer count);

    ShortcutIcon loadIcon(long id);
}