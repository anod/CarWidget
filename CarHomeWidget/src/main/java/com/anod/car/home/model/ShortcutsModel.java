package com.anod.car.home.model;

import android.content.Intent;
import android.util.SparseArray;

public interface ShortcutsModel {

	void createDefaultShortcuts();

	void init();

	void reloadShortcut(int cellId, long shortcutId);

	SparseArray<ShortcutInfo> getShortcuts();

	ShortcutInfo getShortcut(int cellId);

	ShortcutInfo saveShortcutIntent(int cellId, Intent data, boolean isApplicationShortcut);

	void saveShortcut(int cellId, ShortcutInfo info);

	void dropShortcut(int cellId);

	void move(int from, int to);

	int getCount();

	void updateCount(Integer count);
}