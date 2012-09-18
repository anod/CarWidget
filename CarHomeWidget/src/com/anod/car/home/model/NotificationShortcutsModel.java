package com.anod.car.home.model;

import java.util.ArrayList;

import android.content.Context;

public class NotificationShortcutsModel extends AbstractShortcutsModel {

	public NotificationShortcutsModel(Context context) {
		super(context);
	}

	private static final int TOTAL_COUNT = 3;

	@Override
	public void createDefaultShortcuts() {
		// Nothing
	}

	@Override
	protected int getCount() {
		return TOTAL_COUNT;
	}

	@Override
	protected void saveShortcutId(int position, long shortcutId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void dropShortcutId(int position, long shortcutId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ArrayList<Long> loadShortcutIds() {
		// TODO Auto-generated method stub
		return null;
	}



}
