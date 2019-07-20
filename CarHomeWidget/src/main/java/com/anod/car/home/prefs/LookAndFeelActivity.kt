package com.anod.car.home.prefs

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.commit
import androidx.viewpager.widget.ViewPager
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.main.AboutFragment
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.drag.ShortcutDragListener
import com.anod.car.home.prefs.lookandfeel.LookAndFeelMenu
import com.anod.car.home.prefs.lookandfeel.SkinPagerAdapter
import com.anod.car.home.prefs.lookandfeel.WidgetButtonChoiceActivity
import com.anod.car.home.prefs.model.SkinList
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.utils.BitmapLruCache
import com.anod.car.home.utils.forNewShortcut
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.DialogCustom
import kotlinx.android.synthetic.main.activity_lookandfeel.*

class LookAndFeelActivity : CarWidgetActivity(), ViewPager.OnPageChangeListener, WidgetViewBuilder.PendingIntentFactory, ShortcutDragListener.DropCallback {

    private var currentPage: Int = 0

    override val appThemeRes: Int
        get() = theme.transparentResource

    var appWidgetId: Int = 0
        private set

    private val previewInitialized = booleanArrayOf(false, false, false, false, false, false)
    val prefs: WidgetSettings by lazy { WidgetStorage.load(this, appWidgetId) }
    private val skinList: SkinList by lazy { SkinList(prefs.skin, isKeyguard, this) }
    private var bitmapMemoryCache: BitmapLruCache? = null
    private val lookAndFeelMenu: LookAndFeelMenu by lazy { LookAndFeelMenu(this, model) }
    private val model: WidgetShortcutsModel by lazy { WidgetShortcutsModel(App.get(this), appWidgetId) }

    var dragListener: ShortcutDragListener? = null
        private set

    private var adapter: SkinPagerAdapter? = null

    val currentSkinItem: SkinList.Item
        get() = getSkinItem(currentPage)

    private val isKeyguard: Boolean
        get() {
            val widgetOptions = App.provide(this).appWidgetManager.getAppWidgetOptions(appWidgetId)
            val category = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1)
            return category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD
        }

    override fun onDelete(srcCellId: Int): Boolean {
        model.drop(srcCellId)
        refreshSkinPreview()
        return true
    }

    override fun onDrop(srcCellId: Int, dstCellId: Int): Boolean {
        if (srcCellId == dstCellId) {
            return false
        }
        model.move(srcCellId, dstCellId)
        refreshSkinPreview()
        return true
    }

    override fun onDragFinish() {
        dragDeleteShortcut.visibility = View.GONE
    }

    fun onBeforeDragStart() {
        dragDeleteShortcut.tag = ShortcutDragListener.TAG_DELETE_SHORTCUT
        dragDeleteShortcut.setOnDragListener(dragListener)
        dragDeleteShortcut.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        dragDeleteShortcut.startAnimation(animation)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        if (intent == null) {
            AppLog.w("No intent")
            finish()
            return
        }

        appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.e("Invalid app widget id")
            finish()
            return
        }
        setContentView(R.layout.activity_lookandfeel)

        currentPage = skinList.selectedSkinPosition
        dragListener = ShortcutDragListener(dragDeleteBg, this)

        adapter = SkinPagerAdapter(this, skinList.count, supportFragmentManager)
        gallery.adapter = adapter
        gallery.currentItem = currentPage
        gallery.addOnPageChangeListener(this)

        tabs.setupWithViewPager(gallery)

        bitmapMemoryCache = BitmapLruCache(this)

        content.visibility = View.GONE
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_widget -> {
                    invalidateOptionsMenu()
                    content.visibility = View.GONE
                    for (i in 0 until supportFragmentManager.backStackEntryCount) {
                        supportFragmentManager.popBackStack()
                    }
                    true
                }
                R.id.nav_info -> {
                    invalidateOptionsMenu()
                    content.visibility = View.VISIBLE
                    supportFragmentManager.commit {
                        replace(R.id.content, AboutFragment())
                    }
                    true
                }
                R.id.nav_incar -> {
                    invalidateOptionsMenu()
                    content.visibility = View.VISIBLE
                    supportFragmentManager.commit {
                        replace(R.id.content, ConfigurationInCar())
                    }
                    true
                }
                else -> false
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        model.init()
        bitmapMemoryCache?.evictAll()
    }

    fun createBuilder(): WidgetViewBuilder {
        return WidgetViewBuilder(App.get(this), appWidgetId, bitmapMemoryCache, this, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (bottomNavigation.selectedItemId) {
            R.id.nav_widget -> lookAndFeelMenu.onCreateOptionsMenu(menu)
            R.id.nav_incar -> menuInflater.inflate(R.menu.look_n_feel_incar, menu)
            else -> menuInflater.inflate(R.menu.look_n_feel_other, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!lookAndFeelMenu.onOptionsItemSelected(item)) {
            if (item.itemId == R.id.incar_status) {
                DialogCustom(this, theme.alert, R.string.status, R.layout.dialog_incar_status) { view, _ ->
                    InCarStatus(view).apply()
                }.show()
                return true
            }
            return false
        }
        return true
    }

    fun persistPrefs() {
        prefs.apply()
    }

    fun getSkinItem(position: Int): SkinList.Item {
        return skinList[position]
    }

    override fun onPageSelected(position: Int) {
        currentPage = position

        if (!previewInitialized[position]) {
            loader.visibility = View.VISIBLE
        }

        lookAndFeelMenu.refresh()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    fun refreshSkinPreview() {
        AppLog.i("Refresh skin preview")
        adapter!!.refresh()
    }

    override fun createNew(appWidgetId: Int, cellId: Int): PendingIntent {
        val intent = Intent().forNewShortcut(this, appWidgetId, cellId)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent {

        val intent = WidgetButtonChoiceActivity
                .createIntent(appWidgetId, getSkinItem(currentPage).value, buttonId, this)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity
                .createIntent(appWidgetId, getSkinItem(currentPage).value, buttonId, this)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int,
                                shortcutId: Long): PendingIntent {
        val editIntent = ShortcutEditActivity
                .createIntent(this, position, shortcutId, appWidgetId)
        val path = "$appWidgetId/$position"
        val data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path)
        editIntent.data = data
        return PendingIntent
                .getActivity(this, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun onPreviewStart(position: Int) {
        previewInitialized[position] = false
    }

    fun onPreviewCreated(position: Int) {
        if (currentPage == position) {
            loader.visibility = View.GONE
        }
        previewInitialized[position] = true
    }

    fun beforeFinish() {
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE == intent.action && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Provider.requestUpdate(this, intArrayOf(appWidgetId))
        }
        App.provide(this).cleanAppListCache()
    }

    override fun onBackPressed() {
        beforeFinish()
        super.onBackPressed()
    }

}
