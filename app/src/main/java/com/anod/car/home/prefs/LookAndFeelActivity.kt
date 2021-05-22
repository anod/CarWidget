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
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.viewpager.widget.ViewPager
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.appwidget.Provider
import com.anod.car.home.appwidget.WidgetViewBuilder
import info.anodsplace.carwidget.content.backup.BackupManager
import com.anod.car.home.databinding.ActivityLookandfeelBinding
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.main.AboutFragment
import com.anod.car.home.prefs.drag.ShortcutDragListener
import com.anod.car.home.prefs.lookandfeel.LookAndFeelMenu
import com.anod.car.home.prefs.lookandfeel.SkinPagerAdapter
import com.anod.car.home.prefs.lookandfeel.WidgetButtonChoiceActivity
import com.anod.car.home.prefs.model.SkinList
import com.anod.car.home.utils.BitmapLruCache
import com.anod.car.home.utils.forNewShortcut
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.app.DialogCustom

class LookAndFeelActivity : CarWidgetActivity(), ViewPager.OnPageChangeListener, WidgetViewBuilder.PendingIntentFactory, ShortcutDragListener.DropCallback, BackupManager.OnRestore {

    private lateinit var binding: ActivityLookandfeelBinding
    private val currentPage: Int
        get() = binding.gallery.currentItem

    override val appThemeRes: Int
        get() = theme.transparentResource

    var appWidgetId: Int = 0
        private set

    private val previewInitialized = booleanArrayOf(false, false, false, false, false, false, false)
    var prefs: info.anodsplace.carwidget.content.preferences.WidgetSettings? = null
    private var skinList: SkinList? = null
    private var bitmapMemoryCache: BitmapLruCache? = null
    private val lookAndFeelMenu: LookAndFeelMenu by lazy { LookAndFeelMenu(this, model) }
    private val model: WidgetShortcutsModel by lazy { WidgetShortcutsModel(App.get(this), DefaultsResourceProvider(this), appWidgetId) }

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
        binding.dragDeleteShortcut.visibility = View.GONE
    }

    fun onBeforeDragStart() {
        binding.dragDeleteShortcut.tag = ShortcutDragListener.TAG_DELETE_SHORTCUT
        binding.dragDeleteShortcut.setOnDragListener(dragListener)
        binding.dragDeleteShortcut.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.dragDeleteShortcut.startAnimation(animation)
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
        binding = ActivityLookandfeelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = WidgetStorage.load(this, DefaultsResourceProvider(this), appWidgetId)
        skinList = SkinList(prefs!!.skin, isKeyguard, this)
        dragListener = ShortcutDragListener(binding.dragDeleteBg, this)

        adapter = SkinPagerAdapter(this, skinList!!.count, supportFragmentManager)
        binding.gallery.adapter = adapter
        binding.gallery.currentItem = skinList!!.selectedSkinPosition
        binding.gallery.addOnPageChangeListener(this)

        binding.tabs.setupWithViewPager(binding.gallery)

        bitmapMemoryCache = BitmapLruCache(this)

        binding.content.visibility = View.GONE
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            navigate(it.itemId)
        }

        if (savedInstanceState != null) {
            val bottomItemId = savedInstanceState.getInt("bottom_item_id", 0)
            if (bottomItemId > 0) {
                navigate(bottomItemId)
            }
        }
        BroadcastService.registerBroadcastService(applicationContext)
    }

    private fun navigate(@IdRes itemId: Int): Boolean {
        return when (itemId) {
            R.id.nav_widget -> {
                invalidateOptionsMenu()
                binding.gallery.isVisible = true
                binding.content.isVisible = false
                for (i in 0 until supportFragmentManager.backStackEntryCount) {
                    supportFragmentManager.popBackStack()
                }
                true
            }
            R.id.nav_info -> {
                invalidateOptionsMenu()
                binding.gallery.isVisible = false
                binding.content.isVisible = true
                supportFragmentManager.commit {
                    replace(R.id.content, AboutFragment().apply {
                        arguments = bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId)
                    })
                }
                true
            }
            R.id.nav_incar -> {
                invalidateOptionsMenu()
                binding.gallery.isVisible = false
                binding.content.isVisible = true
                supportFragmentManager.commit {
                    replace(R.id.content, ConfigurationInCar())
                }
                true
            }
            else -> false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("bottom_item_id", binding.bottomNavigation.selectedItemId)
        super.onSaveInstanceState(outState)
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
        when (binding.bottomNavigation.selectedItemId) {
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
        prefs!!.apply()
    }

    fun getSkinItem(position: Int): SkinList.Item {
        return skinList!![position]
    }

    override fun onPageSelected(position: Int) {
        if (!previewInitialized[position]) {
            binding.loader.visibility = View.VISIBLE
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
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createSettings(appWidgetId: Int, buttonId: Int): PendingIntent {

        val intent = WidgetButtonChoiceActivity
                .createIntent(appWidgetId, getSkinItem(currentPage).value, buttonId, this)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createInCar(on: Boolean, buttonId: Int): PendingIntent {
        val intent = WidgetButtonChoiceActivity
                .createIntent(appWidgetId, getSkinItem(currentPage).value, buttonId, this)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun createShortcut(intent: Intent, appWidgetId: Int, position: Int,
                                shortcutId: Long): PendingIntent {
        val editIntent = ShortcutEditActivity
                .createIntent(this, position, shortcutId, appWidgetId)
        val path = "$appWidgetId/$position"
        val data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path)
        editIntent.data = data
        return PendingIntent
                .getActivity(this, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun onPreviewStart(position: Int) {
        previewInitialized[position] = false
    }

    fun onPreviewCreated(position: Int) {
        if (currentPage == position) {
            binding.loader.visibility = View.GONE
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

    override fun restoreCompleted() {
        prefs = WidgetStorage.load(this, DefaultsResourceProvider(this), appWidgetId)
        skinList = SkinList(prefs!!.skin, isKeyguard, this).also {
            binding.gallery.currentItem = it.selectedSkinPosition
        }
        model.init()
        bitmapMemoryCache?.evictAll()
        refreshSkinPreview()
    }
}
