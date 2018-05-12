package com.anod.car.home.prefs.lookandfeel

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView

import com.anod.car.home.R
import com.anod.car.home.app.AppCompatGridActivity
import com.anod.car.home.appwidget.WidgetButtonViewBuilder
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.skin.PropertiesFactory
import com.anod.car.home.skin.SkinProperties
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.Utils

import java.util.ArrayList

/**
 * @author alex
 * @date 2015-01-18
 */
class WidgetButtonChoiceActivity : AppCompatGridActivity() {
    private var appWidgetId: Int = 0
    private var skin: String = ""
    private var button: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        appWidgetId = Utils.readAppWidgetId(savedInstanceState, intent)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.d("Invalid AppWidgetId")
            finish()
            return
        }
        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null) {
                skin = extras.getString(EXTRA_SKIN, "")
                button = extras.getInt(EXTRA_BTN, -1)
            }
        } else {
            skin = savedInstanceState.getString(EXTRA_SKIN, "")
            button = savedInstanceState.getInt(EXTRA_BTN, -1)
        }
        if (skin.isEmpty() || button == -1) {
            AppLog.d("Invalid params")
            finish()
            return
        }

        val skinProperties = PropertiesFactory.create(skin)
        val items = createItems(skinProperties)

        val prefs = WidgetStorage.load(this, appWidgetId)
        initCheckedItem(items, prefs)

        listAdapter = ChoiceAdapter(this, items)
    }

    private fun initCheckedItem(items: List<ChoiceAdapter.Item>, prefs: WidgetSettings) {
        val value: Int
        if (button == WidgetButtonViewBuilder.BUTTON_1) {
            value = prefs.widgetButton1
        } else {
            value = prefs.widgetButton2
        }

        for (i in items.indices) {
            val item = items[i]
            item.checked = (item.value == value)
        }

    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val prefs = WidgetStorage.load(this, appWidgetId)
        val item = listAdapter!!.getItem(position) as ChoiceAdapter.Item
        if (button == WidgetButtonViewBuilder.BUTTON_1) {
            prefs.widgetButton1 = item.value
        } else {
            prefs.widgetButton2 = item.value
        }
        prefs.apply()
        finish()
    }

    private fun createItems(skinProperties: SkinProperties): List<ChoiceAdapter.Item> {
        val r = resources
        val items = ArrayList<ChoiceAdapter.Item>(3)
        items.add(ChoiceAdapter.Item(r.getString(R.string.pref_settings_transparent),
                skinProperties.settingsButtonRes, WidgetSettings.WIDGET_BUTTON_SETTINGS))
        items.add(ChoiceAdapter.Item(r.getString(R.string.pref_incar_transparent),
                skinProperties.inCarButtonEnterRes, WidgetSettings.WIDGET_BUTTON_INCAR))
        items.add(ChoiceAdapter.Item(r.getString(R.string.hidden), R.drawable.ic_action_cancel,
                WidgetSettings.WIDGET_BUTTON_HIDDEN))
        return items
    }

    private class ChoiceAdapter(context: Context, items: List<ChoiceAdapter.Item>) : ArrayAdapter<ChoiceAdapter.Item>(context, R.layout.list_item_app, android.R.id.text1) {

        class Item constructor(val title: String, val icon: Int, val value: Int) {
            var checked: Boolean = false

            override fun toString(): String {
                return this.title
            }
        }

        init {
            addAll(items)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)

            val item = getItem(position)
            val icon = view.findViewById<ImageView>(android.R.id.icon)
            icon.setImageResource(item!!.icon)

            return view
        }
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Utils.saveAppWidgetId(outState, appWidgetId)
        outState.putString(EXTRA_SKIN, skin)
        outState.putInt(EXTRA_BTN, button)
    }

    companion object {
        const val EXTRA_SKIN = "skin"
        const val EXTRA_BTN = "btn"

        fun createIntent(appWidgetId: Int, skin: String, buttonId: Int, context: Context): Intent {
            val intent = Intent(context, WidgetButtonChoiceActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.putExtra(WidgetButtonChoiceActivity.EXTRA_SKIN, skin)
            intent.putExtra(WidgetButtonChoiceActivity.EXTRA_BTN, buttonId)
            val path = appWidgetId.toString() + "/widgetButton" + buttonId
            val data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path)
            intent.data = data
            return intent
        }
    }
}
