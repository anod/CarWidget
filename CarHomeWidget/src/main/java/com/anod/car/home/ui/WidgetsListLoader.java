package com.anod.car.home.ui;

import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.WidgetShortcutsModel;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.SparseArray;

/**
 * @author alex
 * @date 5/27/13
 */
public class WidgetsListLoader extends AsyncTaskLoader<SparseArray<SparseArray<ShortcutInfo>>> {

    private final Context mContext;

    public WidgetsListLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public SparseArray<SparseArray<ShortcutInfo>> loadInBackground() {

        int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
        SparseArray<SparseArray<ShortcutInfo>> result
                = new SparseArray<SparseArray<ShortcutInfo>>();

        for (int i = 0; i < appWidgetIds.length; i++) {
            WidgetShortcutsModel model = new WidgetShortcutsModel(mContext, appWidgetIds[i]);
            model.init();

            result.put(appWidgetIds[i], model.getShortcuts());
        }

        return result;
    }

}
