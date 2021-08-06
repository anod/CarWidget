package com.anod.car.home.prefs.lookandfeel

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.app.AppsListActivity
import com.anod.car.home.databinding.ListFooterIconThemesBinding
import com.anod.car.home.utils.Utils
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.chooser.AppsListViewModel
import info.anodsplace.carwidget.chooser.AppsPackageLoader
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.content.forIconTheme
import info.anodsplace.framework.content.startActivitySafely

class IconThemesActivity : AppsListActivity() {

    override fun viewModelFactory(): AppsListViewModel.Factory {
        return AppsListViewModel.Factory(App.get(applicationContext), AppsPackageLoader(this, Intent().forIconTheme()))
    }

    override val isShowTitle: Boolean
        get() = true

    override val headEntries: List<ChooserEntry>
        get() {
            return listOf(ChooserEntry(null, getString(R.string.none)))
        }

    private var currentSelected = 0
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val prefs: WidgetSettings by lazy { WidgetStorage.load(this, DefaultsResourceProvider(this), appWidgetId) }
    private var themePackageName = ""
    private var refresh = false

    override fun inflateFooterView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        val binding = ListFooterIconThemesBinding.inflate(layoutInflater, parent, false)
        return binding.root
    }

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
            startActivitySafely(intent)
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        themePackageName = prefs.iconsTheme
    }

    public override fun onItemsSet(items: List<ChooserEntry>) {
        if (themePackageName.isNotEmpty()) {
            for (i in 1 until items.size) {
                val entry = items[i]
                if (entry.componentName?.packageName == themePackageName) {
                    currentSelected = i
                    break
                }
            }
        }
        gridView.setItemChecked(currentSelected, true)
    }

    override fun onEntryClick(position: Int, entry: ChooserEntry) {
        themePackageName =  entry.componentName?.packageName ?: ""
        gridView.setItemChecked(position, true)
        saveAndClose()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Utils.saveAppWidgetId(outState, appWidgetId)
    }

    private fun saveAndClose() {
        val prevTheme = prefs.iconsTheme
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
            prefs.applyPending()
        }
        finish()
    }

    companion object {
        private const val ADW_ICON_THEME_MARKET_URL = "market://search?q=Icons Pack"
    }
}
