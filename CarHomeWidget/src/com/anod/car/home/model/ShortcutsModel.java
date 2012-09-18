package com.anod.car.home.model;

import android.content.Intent;
import android.util.SparseArray;

public interface ShortcutsModel {

	public abstract void createDefaultShortcuts();

	public abstract void init();

	public abstract void reloadShortcut(int cellId, long shortcutId);

	public abstract SparseArray<ShortcutInfo> getShortcuts();

	public abstract ShortcutInfo getShortcut(int cellId);

	public abstract ShortcutInfo saveShortcutIntent(int cellId, Intent data, boolean isApplicationShortcut);

	public abstract void saveShortcut(int cellId, ShortcutInfo info);

	public abstract void dropShortcut(int cellId);

}