package com.anod.car.home.ui;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.SparseArray;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;

import java.util.HashMap;

/**
 * @author alex
 * @date 5/27/13
 */
public class WidgetsListLoader extends AsyncTaskLoader< HashMap<Integer,SparseArray<ShortcutInfo>> > {
	private final Context mContext;
	private final int[] mAppWidgetIds;

	public WidgetsListLoader(Context context, int[] appWidgetIds) {
		super(context);
		mContext = context;
		mAppWidgetIds = appWidgetIds;
	}

	@Override
	public HashMap<Integer,SparseArray<ShortcutInfo>> loadInBackground() {

		HashMap<Integer,SparseArray<ShortcutInfo>> result = new HashMap<Integer, SparseArray<ShortcutInfo>>();

		for (int i = 0; i < mAppWidgetIds.length; i++) {
			LauncherShortcutsModel model = new LauncherShortcutsModel(mContext,mAppWidgetIds[i]);
			model.init();

			result.put(mAppWidgetIds[i], model.getShortcuts());
		}

		return result;
	}

}
