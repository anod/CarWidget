package com.anod.car.home.appwidget

import android.content.Context
import android.widget.RemoteViews
import androidx.collection.SimpleArrayMap

import com.anod.car.home.R
import info.anodsplace.carwidget.content.db.LauncherSettings
import com.anod.car.home.skin.PropertiesFactory
import info.anodsplace.carwidget.utils.BitmapTransform
import info.anodsplace.carwidget.content.IconTheme
import com.anod.car.home.utils.Utils
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider

class WidgetViewBuilder(private val context: Context,
                        override val appWidgetId: Int,
                        private val bitmapMemoryCache: BitmapLruCache?,
                        private val pendingIntentFactory: PendingIntentFactory,
                        private val widgetButtonAlternativeHidden: Boolean) : WidgetView {

    constructor(context: Context, appWidgetId: Int, pendingIntentFactory: PendingIntentFactory)
            : this(context, appWidgetId, null, pendingIntentFactory, false)

    private var overrideSkin: String? = null

    private val prefs: WidgetSettings by lazy { WidgetStorage.load(context, DefaultsResourceProvider(context), appWidgetId) }
    private val shortcutsModel: WidgetShortcutsModel by lazy { WidgetShortcutsModel(context, DefaultsResourceProvider(context), appWidgetId) }
    private var shortcutViewBuilder: ShortcutViewBuilder? = null
    private var bitmapTransform: BitmapTransform? = null
    private var widgetButtonViewBuilder: WidgetButtonViewBuilder? = null

    override fun init(overrideSkin: String?) {
        this.overrideSkin = overrideSkin

        if (prefs.isFirstTime) {
            shortcutsModel.createDefaultShortcuts()
            prefs.isFirstTime = false
            prefs.apply()
        }

        shortcutsModel.init()

        bitmapTransform = BitmapTransform(context)
        shortcutViewBuilder = ShortcutViewBuilder(context, appWidgetId, pendingIntentFactory).also {
            it.bitmapMemoryCache = bitmapMemoryCache
        }

        widgetButtonViewBuilder = WidgetButtonViewBuilder(context, prefs, pendingIntentFactory, appWidgetId).also {
            it.alternativeHidden = widgetButtonAlternativeHidden
        }
        refreshIconTransform()
    }

    override fun refreshIconTransform() {
        bitmapTransform?.let {
            applyIconTransform(it, prefs)
        }
    }

    override fun create(): RemoteViews {
        val shortcuts = shortcutsModel.shortcuts
        val r = context.resources
        val skinName = if (overrideSkin == null) prefs.skin else overrideSkin
        val scaledDensity = r.displayMetrics.scaledDensity

        val skinProperties = PropertiesFactory.create(skinName!!)
        val iconPaddingRes = skinProperties.iconPaddingRes
        if (iconPaddingRes > 0 && !prefs.isTitlesHide) {
            val iconPadding = r.getDimension(iconPaddingRes).toInt()
            bitmapTransform!!.paddingBottom = iconPadding
        }

        val views = RemoteViews(context.packageName,
                skinProperties.getLayout(shortcuts.size()))

        widgetButtonViewBuilder!!.setup(skinProperties, views)

        views.setInt(R.id.container, "setBackgroundColor", prefs.backgroundColor)
        bitmapTransform!!.iconProcessor = skinProperties.iconProcessor

        val themePackage = prefs.iconsTheme
        val themeIcons = if (themePackage.isEmpty()) null else loadThemeIcons(themePackage)

        shortcutViewBuilder!!.init(scaledDensity, skinProperties, themeIcons, prefs, shortcutsModel,
                bitmapTransform!!)

        val totalRows = shortcuts.size() / 2
        for (rowNum in 0 until totalRows) {
            val firstBtn = rowNum * 2
            val secondBtn = firstBtn + 1

            shortcutViewBuilder?.run {
                fill(views, firstBtn, btnIds[firstBtn], textIds[firstBtn])
                fill(views, secondBtn, btnIds[secondBtn], textIds[secondBtn])
            }
        }

        return views
    }

    override fun loadThemeIcons(themePackage: String): IconTheme? {
        val shortcuts = shortcutsModel.shortcuts

        val theme = IconTheme(context, themePackage)
        if (!theme.loadThemeResources()) {
            return null
        }

        val cmpMap = SimpleArrayMap<String, Int>(shortcuts.size())
        for (cellId in 0 until shortcuts.size()) {
            val info = shortcutsModel.get(cellId)
            if (info == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon) {
                continue
            }
            cmpMap.put(info.intent.component!!.className, cellId)
        }
        theme.loadFromXml(cmpMap)
        return theme
    }

    companion object {

        private val textIds = intArrayOf(
            R.id.btn_text0, R.id.btn_text1, //2
            R.id.btn_text2, R.id.btn_text3, //4
            R.id.btn_text4, R.id.btn_text5, //6
            R.id.btn_text6, R.id.btn_text7, //8
            R.id.btn_text8, R.id.btn_text9  //10
        )

        internal val btnIds = intArrayOf(
            R.id.btn0, R.id.btn1, //2
            R.id.btn2, R.id.btn3, //4
            R.id.btn4, R.id.btn5, //6
            R.id.btn6, R.id.btn7, //8
            R.id.btn8, R.id.btn9  //10
        )

        private fun applyIconTransform(bt: BitmapTransform, prefs: WidgetSettings) {
            if (prefs.isIconsMono) {
                bt.applyGrayFilter = true
                if (prefs.iconsColor != null) {
                    bt.tintColor = prefs.iconsColor
                }
            }

            val iconScale = Utils.calcIconsScale(prefs.iconsScale)
            if (iconScale > 1.0f) {
                bt.scaleSize = iconScale
            }
            bt.rotateDirection = prefs.iconsRotate
        }
    }
}

