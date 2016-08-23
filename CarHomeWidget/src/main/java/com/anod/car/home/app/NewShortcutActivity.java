package com.anod.car.home.app;

import com.anod.car.home.appwidget.Provider;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.WidgetShortcutsModel;
import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.ShortcutPicker;
import com.anod.car.home.utils.Utils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * @author alex
 * @date 2014-10-24
 */
public class NewShortcutActivity extends Activity implements ShortcutPicker.Handler {

    private int mAppWidgetId;

    private ShortcutPicker mShortcutPicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getIntent());

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.e("AppWidgetId required");
            finish();
            return;
        }

        Intent defaultResultValue = new Intent();
        defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(Activity.RESULT_OK, defaultResultValue);

        WidgetShortcutsModel model = WidgetShortcutsModel.init(this, mAppWidgetId);
        mShortcutPicker = new ShortcutPicker(model, this, this);
        int cellId = mShortcutPicker.onRestoreInstanceState(savedInstanceState, getIntent());
        if (cellId == ShortcutPicker.INVALID_CELL_ID) {
            AppLog.e("cellId required");
            finish();
            return;
        }

        mShortcutPicker.showActivityPicker(cellId);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Utils.saveAppWidgetId(outState, mAppWidgetId);
        mShortcutPicker.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mShortcutPicker.onActivityResult(requestCode, resultCode, data)) {
            finish();
        }
    }

    @Override
    public void onAddShortcut(int cellId, Shortcut info) {
        Provider.getInstance().requestUpdate(this, mAppWidgetId);
        finish();
    }

    @Override
    public void onEditComplete(int cellId) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
