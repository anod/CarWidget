package com.anod.car.home.prefs

import androidx.viewpager.widget.ViewPager
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.prefs.drag.ShortcutDragListener
import info.anodsplace.carwidget.content.backup.BackupManager
import org.koin.core.component.KoinComponent

abstract class LookAndFeelActivityOld : CarWidgetActivity(), ViewPager.OnPageChangeListener, ShortcutDragListener.DropCallback, BackupManager.OnRestore, KoinComponent {
//
//    private lateinit var binding: ActivityLookandfeelBinding
//    private val currentPage: Int
//        get() = binding.gallery.currentItem
//
//    override val appThemeRes: Int
//        get() = R.style.AppTheme_Transparent
//
//    var appWidgetId: Int = 0
//        private set
//
//    private val viewModel: SkinPreviewViewModel by viewModels()
//    private val previewInitialized = booleanArrayOf(false, false, false, false, false, false, false)
//    private val lookAndFeelMenu: LookAndFeelMenu by lazy { LookAndFeelMenu(this, viewModel.shortcuts) }
//
//    var dragListener: ShortcutDragListener? = null
//        private set
//
//    private var adapter: SkinPagerAdapter? = null
//
//    val currentSkinItem: SkinList.Item
//        get() = getSkinItem(currentPage)
//
//    override fun onDelete(srcCellId: Int): Boolean {
//        viewModel.shortcuts.drop(srcCellId)
//        refreshSkinPreview()
//        return true
//    }
//
//    override fun onDrop(srcCellId: Int, dstCellId: Int): Boolean {
//        if (srcCellId == dstCellId) {
//            return false
//        }
//        viewModel.shortcuts.move(srcCellId, dstCellId)
//        refreshSkinPreview()
//        return true
//    }
//
//    override fun onDragFinish() {
//        binding.dragDeleteShortcut.visibility = View.GONE
//    }
//
//    fun onBeforeDragStart() {
//        binding.dragDeleteShortcut.tag = ShortcutDragListener.TAG_DELETE_SHORTCUT
//        binding.dragDeleteShortcut.setOnDragListener(dragListener)
//        binding.dragDeleteShortcut.visibility = View.VISIBLE
//        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
//        binding.dragDeleteShortcut.startAnimation(animation)
//    }
//
//    public override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (intent == null) {
//            AppLog.w("No intent")
//            finish()
//            return
//        }
//
//        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
//        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
//            AppLog.e("Invalid app widget id")
//            finish()
//            return
//        }
//        binding = ActivityLookandfeelBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        dragListener = ShortcutDragListener(binding.dragDeleteBg, this)
//
//        adapter = SkinPagerAdapter(this, viewModel.skinList.count, supportFragmentManager)
//        binding.gallery.adapter = adapter
//        binding.gallery.currentItem = viewModel.skinList.selectedSkinPosition
//        binding.gallery.addOnPageChangeListener(this)
//
//        binding.tabs.setupWithViewPager(binding.gallery)
//
//        binding.content.visibility = View.GONE
//        binding.bottomNavigation.setOnNavigationItemSelectedListener {
//            navigate(it.itemId)
//        }
//
//        binding.toolbar.setTitle(R.string.app_name)
//        binding.toolbar.setOnMenuItemClickListener {
//            lookAndFeelMenu.onOptionsItemSelected(it)
//        }
//
//        if (savedInstanceState != null) {
//            val bottomItemId = savedInstanceState.getInt("bottom_item_id", 0)
//            if (bottomItemId > 0) {
//                navigate(bottomItemId)
//            }
//        } else {
//            lookAndFeelMenu.onCreateOptionsMenu(binding.toolbar, viewModel.widgetSettings.isIconsMono)
//        }
//        BroadcastService.registerBroadcastService(applicationContext)
//    }
//
//    private fun navigate(@IdRes itemId: Int): Boolean {
//        return when (itemId) {
//            R.id.nav_widget -> {
//                lookAndFeelMenu.onCreateOptionsMenu(binding.toolbar, viewModel.widgetSettings.isIconsMono)
//                binding.gallery.isVisible = true
//                binding.content.isVisible = false
//                for (i in 0 until supportFragmentManager.backStackEntryCount) {
//                    supportFragmentManager.popBackStack()
//                }
//                true
//            }
//            R.id.nav_info -> {
//                binding.toolbar.menu.clear()
//                binding.gallery.isVisible = false
//                binding.content.isVisible = true
//                supportFragmentManager.commit {
//                    replace(R.id.content, AboutScreenFragment().apply {
//                        arguments = bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId)
//                    })
//                }
//                true
//            }
//            R.id.nav_incar -> {
//                binding.toolbar.menu.clear()
//                binding.gallery.isVisible = false
//                binding.content.isVisible = true
//                supportFragmentManager.commit {
//                    replace(R.id.content, InCarScreenFragment().apply {
//                        arguments = bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId)
//                    })
//                }
//                true
//            }
//            else -> false
//        }
//    }
//
//    @SuppressLint("MissingSuperCall")
//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putInt("bottom_item_id", binding.bottomNavigation.selectedItemId)
//        super.onSaveInstanceState(outState)
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        viewModel.shortcuts.init()
//    }
//
//    fun persistPrefs() {
//        viewModel.widgetSettings.apply()
//    }
//
//    fun getSkinItem(position: Int): SkinList.Item {
//        return viewModel.skinList[position]
//    }
//
//    override fun onPageSelected(position: Int) {
//        if (!previewInitialized[position]) {
//            binding.loader.visibility = View.VISIBLE
//        }
//
//        lookAndFeelMenu.refresh()
//    }
//
//    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//    }
//
//    override fun onPageScrollStateChanged(state: Int) {
//    }
//
//    fun refreshSkinPreview() {
//        AppLog.i("Refresh skin preview")
//        adapter!!.refresh()
//    }
//
//    fun onPreviewStart(position: Int) {
//        previewInitialized[position] = false
//    }
//
//    fun onPreviewCreated(position: Int) {
//        if (currentPage == position) {
//            binding.loader.visibility = View.GONE
//        }
//        previewInitialized[position] = true
//    }
//
//    fun beforeFinish() {
//        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE == intent.action && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
//            Provider.requestUpdate(this, intArrayOf(appWidgetId))
//        }
//    }
//
//    override fun onBackPressed() {
//        beforeFinish()
//        super.onBackPressed()
//    }
//
//    override fun restoreCompleted() {
////        prefs = WidgetStorage.load(this, DefaultsResourceProvider(this), appWidgetId)
////        skinList = SkinList(prefs!!.skin, isKeyguard, this).also {
////            binding.gallery.currentItem = it.selectedSkinPosition
////        }
//        viewModel.shortcuts.init()
////        bitmapMemoryCache?.evictAll()
//        refreshSkinPreview()
//    }
}
