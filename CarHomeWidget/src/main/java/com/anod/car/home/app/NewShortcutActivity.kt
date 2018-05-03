package com.anod.car.home.app

import com.anod.car.home.appwidget.Provider
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.WidgetShortcutsModel
import info.anodsplace.android.log.AppLog
import com.anod.car.home.utils.ShortcutPicker
import com.anod.car.home.utils.Utils

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Window

/**
 * @author alex
 * @date 2014-10-24
 */
class NewShortcutActivity : Activity(), ShortcutPicker.Handler {

    private var appWidgetId: Int = 0

    private var shortcutPicker: ShortcutPicker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        appWidgetId = Utils.readAppWidgetId(savedInstanceState, intent)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.e("AppWidgetId required")
            finish()
            return
        }

        val defaultResultValue = Intent()
        defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, defaultResultValue)

        val model = WidgetShortcutsModel.init(this, appWidgetId)
        shortcutPicker = ShortcutPicker(model, this, this)
        val cellId = shortcutPicker!!.onRestoreInstanceState(savedInstanceState, intent)
        if (cellId == ShortcutPicker.INVALID_CELL_ID) {
            AppLog.e("cellId required")
            finish()
            return
        }

        shortcutPicker!!.showActivityPicker(cellId)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Utils.saveAppWidgetId(outState, appWidgetId)
        shortcutPicker!!.onSaveInstanceState(outState)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!shortcutPicker!!.onActivityResult(requestCode, resultCode, data)) {
            finish()
        }
    }

    override fun onAddShortcut(cellId: Int, info: Shortcut) {
        Provider.getInstance().requestUpdate(this, appWidgetId)
        finish()
    }

    override fun onEditComplete(cellId: Int) {
        finish()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

}
