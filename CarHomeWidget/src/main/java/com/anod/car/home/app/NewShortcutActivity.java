package com.anod.car.home.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.anod.car.home.Provider;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.ShortcutPicker;
import com.anod.car.home.utils.Utils;

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
        int cellId = getIntent().getExtras().getInt(ShortcutPicker.EXTRA_CELL_ID, ShortcutPicker.INVALID_CELL_ID);
        if (cellId == ShortcutPicker.INVALID_CELL_ID) {
            AppLog.e("cellId required");
            finish();
            return;
        }
        Intent defaultResultValue = new Intent();
        defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(Activity.RESULT_OK, defaultResultValue);

        WidgetShortcutsModel model = new WidgetShortcutsModel(this, mAppWidgetId);
        model.init();
        mShortcutPicker = new ShortcutPicker(model, this, this);
        mShortcutPicker.onRestoreInstanceState(savedInstanceState);

        mShortcutPicker.showActivityPicker(cellId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mShortcutPicker.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mShortcutPicker.onActivityResult(requestCode, resultCode, data)) {
            finish();
        }
    }

    @Override
    public void onAddShortcut(int cellId, ShortcutInfo info) {
        Provider.getInstance().requestUpdate(this,mAppWidgetId);
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
