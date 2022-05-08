package com.anod.car.home.appwidget

import android.content.Context
import android.widget.RemoteViews
import androidx.collection.SimpleArrayMap
import com.anod.car.home.R
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.IconTheme
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.graphics.BitmapTransform
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

class WidgetViewBuilder(
    context: Context,
    iconLoader: ShortcutIconLoader,
    appWidgetId: Int,
    widgetSettings: WidgetInterface,
    inCarSettings: InCarInterface,
    koin: Koin,
    shortcutsModel: Shortcuts,
    private val bitmapMemoryCache: BitmapLruCache?,
    pendingIntentFactory: PendingIntentFactory,
    private val widgetButtonAlternativeHidden: Boolean,
    override var overrideSkin: String?,
) : WidgetView {

    private val bitmapTransform = BitmapTransform(context)
    private var instance: WidgetView = Instance(
        overrideSkin = overrideSkin,
        context = context,
        prefs = widgetSettings,
        shortcutsModel = shortcutsModel,
        bitmapTransform = bitmapTransform,
        shortcutViewBuilder = ShortcutViewBuilder(context, appWidgetId, pendingIntentFactory, iconLoader).also {
            it.bitmapMemoryCache = bitmapMemoryCache
        },
        widgetButtonViewBuilder = WidgetButtonViewBuilder(widgetSettings, pendingIntentFactory, inCarSettings, appWidgetId).also {
            it.alternativeHidden = widgetButtonAlternativeHidden
        },
        koin = koin
    )

    override fun refreshIconTransform() {
        instance.refreshIconTransform()
    }

    override suspend fun create(): RemoteViews {
        return instance.create()
    }

    override fun loadThemeIcons(themePackage: String): IconTheme? {
        return instance.loadThemeIcons(themePackage)
    }

    class Instance(
        override var overrideSkin: String?,
        private val context: Context,
        private val prefs: WidgetInterface,
        private val shortcutsModel: Shortcuts,
        private var shortcutViewBuilder: ShortcutViewBuilder,
        private var bitmapTransform: BitmapTransform,
        private var widgetButtonViewBuilder: WidgetButtonViewBuilder,
        private val koin: Koin
    ): WidgetView {

        override fun refreshIconTransform() {
            applyIconTransform(bitmapTransform, prefs)
        }

        override suspend fun create(): RemoteViews = withContext(Dispatchers.Default) {
            if (prefs.isFirstTime) {
                shortcutsModel.createDefaultShortcuts()
                prefs.isFirstTime = false
                prefs.applyPending()
            }

            shortcutsModel.init()
            refreshIconTransform()

            val r = context.resources
            val skinName = overrideSkin ?: prefs.skin
            val scaledDensity = r.displayMetrics.scaledDensity

            val skinProperties: SkinProperties = koin.get { parametersOf(skinName) }
            val iconPaddingRes = skinProperties.iconPaddingRes
            if (iconPaddingRes > 0 && !prefs.isTitlesHide) {
                val iconPadding = r.getDimension(iconPaddingRes).toInt()
                bitmapTransform.paddingBottom = iconPadding
            }

            val views = RemoteViews(context.packageName, skinProperties.getLayout(shortcutsModel.count))

            widgetButtonViewBuilder.setup(skinProperties, views)

            views.setInt(R.id.container, "setBackgroundColor", prefs.backgroundColor)
            bitmapTransform.iconProcessor = skinProperties.iconProcessor

            val themePackage = prefs.iconsTheme
            val themeIcons = if (themePackage.isEmpty()) null else loadThemeIcons(themePackage)

            shortcutViewBuilder.init(scaledDensity, skinProperties, themeIcons, prefs, shortcutsModel,
                    bitmapTransform)

            val totalRows = shortcutsModel.count / 2
            for (rowNum in 0 until totalRows) {
                val firstBtn = rowNum * 2
                val secondBtn = firstBtn + 1

                shortcutViewBuilder.run {
                    fill(views, firstBtn, btnIds[firstBtn], textIds[firstBtn])
                    fill(views, secondBtn, btnIds[secondBtn], textIds[secondBtn])
                }
            }

            return@withContext views
        }

        override fun loadThemeIcons(themePackage: String): IconTheme? {
            val shortcuts = shortcutsModel.shortcuts

            val theme = IconTheme(context, themePackage)
            if (!theme.loadThemeResources()) {
                return null
            }

            val cmpMap = SimpleArrayMap<String, Int>(shortcuts.size)
            for (cellId in 0 until shortcuts.size) {
                val info = shortcutsModel.get(cellId)
                if (info == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon) {
                    continue
                }
                cmpMap.put(info.intent.component!!.className, cellId)
            }
            theme.loadFromXml(cmpMap)
            return theme
        }
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

        private fun applyIconTransform(bt: BitmapTransform, prefs: WidgetInterface) {
            if (prefs.isIconsMono) {
                bt.applyGrayFilter = true
                if (prefs.iconsColor != null) {
                    bt.tintColor = prefs.iconsColor
                }
            }

            val iconScale = WidgetInterface.convertIconsScale(prefs.iconsScale)
            if (iconScale > 1.0f) {
                bt.scaleSize = iconScale
            }
            bt.rotateDirection = prefs.iconsRotate
        }
    }
}