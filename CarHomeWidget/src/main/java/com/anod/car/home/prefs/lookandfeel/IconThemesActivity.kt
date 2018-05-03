package com.anod.car.home.prefs.lookandfeel

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ListView

import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.appscache.AppsCacheActivity
import com.anod.car.home.model.AppsList
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import info.anodsplace.android.log.AppLog
import com.anod.car.home.utils.IconPackUtils
import com.anod.car.home.utils.Utils

import java.util.ArrayList

class IconThemesActivity : AppsCacheActivity() {

    override val footerViewId: Int
        get() = R.layout.list_footer_icon_themes

    override val isShowTitle: Boolean
        get() = true

    override val isRefreshCache: Boolean
        get() = refresh

    override val headEntries: List<AppsList.Entry>
        get() {
            val head = ArrayList<AppsList.Entry>(1)
            val none = AppsList.Entry()
            none.title = getString(R.string.none)
            head.add(none)
            return head
        }

    private var currentSelected = 0
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val prefs: WidgetSettings by lazy { WidgetStorage.load(this, appWidgetId) }
    private var themePackageName = ""
    private var refresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gridView.choiceMode = ListView.CHOICE_MODE_SINGLE
        appWidgetId = Utils.readAppWidgetId(savedInstanceState, intent)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.d("Invalid AppWidgetId")
            finish()
            return
        }
        refresh = false

        findViewById<Button>(R.id.btn_download).setOnClickListener {
            val uri = Uri.parse(ADW_ICON_THEME_MARKET_URL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            refresh = true
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
    }

    override fun onResumeImpl() {
        themePackageName = prefs.iconsTheme ?: ""
    }

    public override fun onItemsSet(items: List<AppsList.Entry>) {
        if (themePackageName.isNotEmpty()) {
            for (i in 1 until items.size) {
                val entry = items[i]
                if (entry.componentName != null && entry.componentName.packageName == themePackageName) {
                    currentSelected = i
                    break
                }
            }
        }
        gridView.setItemChecked(currentSelected, true)
    }

    override fun onIntentFilterInit(intent: Intent) {
        IconPackUtils.fillAdwThemeIntent(intent)
    }

    override fun onEntryClick(position: Int, entry: AppsList.Entry) {
        themePackageName = if (entry.componentName == null)
            ""
        else
            entry.componentName.packageName
        gridView.setItemChecked(position, true)
        saveAndClose()
    }

    override fun createAppList(context: Context): AppsList {
        return App.provide(context).iconThemesCache
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Utils.saveAppWidgetId(outState, appWidgetId)
    }

    private fun saveAndClose() {
        val prevTheme = prefs.iconsTheme ?: ""
        var update = false
        if (themePackageName.isEmpty() && prevTheme.isNotEmpty()) {
            update = true
        } else if (themePackageName.isNotEmpty() && prevTheme.isEmpty()) {
            update = true
        } else if (themePackageName.isNotEmpty() && themePackageName != prevTheme) {
            update = true
        }
        if (update) {
            prefs.iconsTheme = themePackageName
            prefs.apply()
        }
        finish()
    }

    companion object {
        private const val ADW_ICON_THEME_MARKET_URL = "market://search?q=Icons Pack"
    }
}
