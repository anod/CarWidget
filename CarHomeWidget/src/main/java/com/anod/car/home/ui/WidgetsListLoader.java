package com.anod.car.home.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.SparseArray;

import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;

/**
 * @author alex
 * @date 5/27/13
 */
public class WidgetsListLoader extends AsyncTaskLoader< SparseArray<SparseArray<ShortcutInfo>> > {
	private final Context mContext;
	private final int[] mAppWidgetIds;

	public WidgetsListLoader(Context context, int[] appWidgetIds) {
		super(context);
		mContext = context;
		mAppWidgetIds = appWidgetIds;
	}

	@Override
	public SparseArray<SparseArray<ShortcutInfo>> loadInBackground() {

		SparseArray<SparseArray<ShortcutInfo>> result = new SparseArray<SparseArray<ShortcutInfo>>();

		for (int i = 0; i < mAppWidgetIds.length; i++) {
			LauncherShortcutsModel model = new LauncherShortcutsModel(mContext,mAppWidgetIds[i]);
			model.init();

			result.put(mAppWidgetIds[i], model.getShortcuts());
		}

		return result;
	}

}
