package com.anod.car.home.prefs

import android.annotation.TargetApi
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView

import com.anod.car.home.appwidget.Provider
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.drawer.NavigationDrawer
import com.anod.car.home.drawer.NavigationList
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.drag.ShortcutDragListener
import com.anod.car.home.prefs.lookandfeel.LookAndFeelMenu
import com.anod.car.home.prefs.lookandfeel.SkinPagerAdapter
import com.anod.car.home.prefs.lookandfeel.WidgetButtonChoiceActivity
import com.anod.car.home.prefs.model.SkinList
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import info.anodsplace.android.log.AppLog
import com.anod.car.home.utils.BitmapLruCache
import com.anod.car.home.utils.HtmlCompat
import com.anod.car.home.utils.IntentUtils

class LookAndFeelActivity : CarWidgetActivity(), ViewPager.OnPageChangeListener, WidgetViewBuilder.PendingIntentFactory, ShortcutDragListener.DropCallback {

    private var currentPage: Int = 0

    var appWidgetId: Int = 0
        private set

    private val previewInitialized = booleanArrayOf(false, false, false, false, false, false)
    val prefs: WidgetSettings by lazy { WidgetStorage.load(this, appWidgetId) }
    private val skinList: SkinList by lazy { SkinList.newInstance(prefs.skin, isKeyguard, this) }
    private var bitmapMemoryCache: BitmapLruCache? = null
    private val textView: TextView by lazy { findViewById<TextView>(R.id.skin_info) }
    private val gallery: ViewPager by lazy { findViewById<ViewPager>(R.id.gallery) }
    private val loaderView: View by lazy { findViewById<View>(R.id.loading) }
    private val lookAndFeelMenu: LookAndFeelMenu by lazy { LookAndFeelMenu(this, model) }
    private val drawer: NavigationDrawer by lazy { NavigationDrawer(this, appWidgetId) }
    private val model: WidgetShortcutsModel by lazy { WidgetShortcutsModel(App.get(this), appWidgetId) }

    var dragListener: ShortcutDragListener? = null
        private set

    private var adapter: SkinPagerAdapter? = null

    val currentSkinItem: SkinList.Item
        get() = getSkinItem(currentPage)

    private val isKeyguard: Boolean
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return false
            }
            val widgetOptions = AppWidgetManager.getInstance(this)
                    .getAppWidgetOptions(appWidgetId)
            val category = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1)
            return category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD
        }

    override fun onDelete(srcCellId: Int): Boolean {
        model.dropShortcut(srcCellId)
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
        val deleteView = findViewById<View>(R.id.drag_delete_shortcut)
        deleteView.visibility = View.GONE
    }

    fun onBeforeDragStart() {
        val deleteView = findViewById<View>(R.id.drag_delete_shortcut)
        deleteView.tag = ShortcutDragListener.TAG_DELETE_SHORTCUT
        deleteView.setOnDragListener(dragListener)
        deleteView.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        deleteView.startAnimation(animation)
    }

    override val isTransparentAppTheme = true

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
        drawer.setSelected(NavigationList.ID_CURRENT_WIDGET)
        textView.movementMethod = LinkMovementMethod.getInstance()
        dragListener = ShortcutDragListener(this, this)

        adapter = SkinPagerAdapter(this, skinList.count, supportFragmentManager)
        gallery.adapter = adapter
        gallery.currentItem = currentPage
        gallery.addOnPageChangeListener(this)

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(gallery)

        bitmapMemoryCache = BitmapLruCache(this)
        showText(currentPage)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawer.syncState()
    }


    public override fun onResume() {
        super.onResume()
        model.init()
        bitmapMemoryCache?.evictAll()
        drawer.refresh()
    }

    fun createBuilder(): WidgetViewBuilder {
        val builder = WidgetViewBuilder(App.get(this))
        builder.setPendingIntentHelper(this)
                .setAppWidgetId(appWidgetId)
                .setBitmapMemoryCache(bitmapMemoryCache)
        builder.setWidgetButtonAlternativeHidden(true)
        return builder
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        lookAndFeelMenu.onCreateOptionsMenu(menu)
        // Calling super after populating the menu is necessary here to ensure
        // that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (drawer.onOptionsItemSelected(item)) {
            true
        } else lookAndFeelMenu.onOptionsItemSelected(item)
    }

    fun persistPrefs() {
        prefs.apply()
    }

    fun getSkinItem(position: Int): SkinList.Item {
        return skinList.get(position)
    }

    override fun onPageSelected(position: Int) {
        currentPage = position
        showText(position)

        if (!previewInitialized[position]) {
            loaderView.visibility = View.VISIBLE
        }

        lookAndFeelMenu.refreshTileColorButton()
    }

    private fun showText(position: Int) {
        val textRes = getSkinItem(position).textRes
        if (textRes > 0) {
            val text = HtmlCompat.fromHtml(getString(textRes))
            textView.text = text
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // Auto-generated method stub

    }

    override fun onPageScrollStateChanged(state: Int) {
        // Auto-generated method stub

    }

    fun refreshSkinPreview() {
        AppLog.d("Refresh Skin Requested")
        adapter!!.refresh()
    }

    override fun createNew(appWidgetId: Int, cellId: Int): PendingIntent {
        val intent = IntentUtils.createNewShortcutIntent(this, appWidgetId, cellId)
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
        val path = appWidgetId.toString() + "/" + position
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
            loaderView.visibility = View.GONE
        }
        previewInitialized[position] = true
    }

    fun beforeFinish() {
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE == intent.action && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Provider.getInstance().requestUpdate(this, appWidgetId)
        }
        App.provide(this).cleanAppListCache()
    }

    override fun onBackPressed() {
        beforeFinish()
        super.onBackPressed()
    }

}
