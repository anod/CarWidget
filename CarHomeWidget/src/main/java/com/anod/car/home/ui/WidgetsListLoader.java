package com.anod.car.home.ui;

import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.WidgetShortcutsModel;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.SparseArray;

/**
 * @author alex
 * @date 5/27/13
 */
class WidgetsListLoader extends AsyncTaskLoader<WidgetsListLoader.Result> {

    private final Context mContext;

    static class Result
    {
        SparseArray<SparseArray<Shortcut>> large;
        int[] shortcuts;
    }

    WidgetsListLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Result loadInBackground() {

        int[] appWidgetIds = WidgetHelper.getLargeWidgetIds(mContext);
        SparseArray<SparseArray<Shortcut>> large = new SparseArray<>();

        for (int i = 0; i < appWidgetIds.length; i++) {
            WidgetShortcutsModel model = new WidgetShortcutsModel(mContext, appWidgetIds[i]);
            model.init();

            large.put(appWidgetIds[i], model.getShortcuts());
        }

        Result result = new Result();
        result.large = large;
        result.shortcuts = WidgetHelper.getShortcutWidgetIds(mContext);
        return result;
    }

}
