package info.anodsplace.carwidget.appwidget

import android.content.Context
import android.widget.RemoteViews
import androidx.collection.SimpleArrayMap
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.IconTheme
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.graphics.BitmapTransform
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DummyWidgetView {
    suspend fun create(): RemoteViews
}

interface WidgetView : DummyWidgetView

interface ShortcutViewBuilderInstance {
    var scaledDensity: Float
    var iconTheme: IconTheme?
    suspend fun fill(views: RemoteViews, position: Int, resBtn: Int, resText: Int)
}

class SkinWidgetView(
    private val skinProperties: SkinProperties,
    private val shortcuts: Map<Int, Shortcut?>,
    private val context: Context,
    private val widgetSettings: WidgetInterface,
    appWidgetId: Int,
    inCarMode: Boolean,
    pendingIntentFactory: PendingIntentFactory,
    inCarSettings: InCarInterface,
    iconLoader: ShortcutIconLoader,
    bitmapMemoryCache: BitmapLruCache? = null,
    widgetButtonAlternativeHidden: Boolean = false,
    private val bitmapTransform: BitmapTransform = BitmapTransform(context),
) : WidgetView {

    private val shortcutViewBuilder = ShortcutViewBuilder(
        skinProperties = skinProperties,
        context = context,
        appWidgetId = appWidgetId,
        pendingIntentFactory = pendingIntentFactory,
        iconLoader=  iconLoader,
        prefs = widgetSettings,
        shortcuts = shortcuts,
        bitmapTransform = bitmapTransform,
        bitmapMemoryCache = bitmapMemoryCache
    )

    private val widgetButtonViewBuilder = WidgetButtonViewBuilder(
        skinProperties = skinProperties,
        inCarMode = inCarMode,
        prefs = widgetSettings,
        pendingIntentFactory = pendingIntentFactory,
        inCarSettings = inCarSettings,
        appWidgetId = appWidgetId,
        alternativeHidden = widgetButtonAlternativeHidden
    )

    override suspend fun create(): RemoteViews = withContext(Dispatchers.Default) {

        applyIconTransform(bitmapTransform, widgetSettings)

        val r = context.resources
        val scaledDensity = r.displayMetrics.scaledDensity

        val iconPaddingRes = skinProperties.iconPaddingRes
        if (iconPaddingRes > 0 && !widgetSettings.isTitlesHide) {
            val iconPadding = r.getDimension(iconPaddingRes).toInt()
            bitmapTransform.paddingBottom = iconPadding
        }

        val views = RemoteViews(context.packageName, skinProperties.getLayout(shortcuts.size))

        widgetButtonViewBuilder.apply(views)

        if (skinProperties.backgroundId != 0) {
            views.setInt(skinProperties.backgroundId, "setBackgroundColor", widgetSettings.backgroundColor)
        } else {
            views.setInt(
                skinProperties.containerId,
                "setBackgroundColor",
                widgetSettings.backgroundColor
            )
        }
        bitmapTransform.iconProcessor = skinProperties.iconProcessor

        val themePackage = widgetSettings.iconsTheme
        val themeIcons = if (themePackage.isEmpty()) null else loadThemeIcons(themePackage, shortcuts, context)

        shortcutViewBuilder.scaledDensity = scaledDensity
        shortcutViewBuilder.iconTheme = themeIcons

        val totalRows = shortcuts.size / 2
        for (rowNum in 0 until totalRows) {
            val firstBtn = rowNum * 2
            val secondBtn = firstBtn + 1

            shortcutViewBuilder.run {
                fill(views, firstBtn, skinProperties.shortcutIdAt(firstBtn), skinProperties.shortcutTextIdAt(firstBtn))
                fill(views, secondBtn, skinProperties.shortcutIdAt(secondBtn), skinProperties.shortcutTextIdAt(secondBtn))
            }
        }

        return@withContext views
    }

    companion object {
        private fun loadThemeIcons(themePackage: String, shortcuts: Map<Int, Shortcut?>, context: Context): IconTheme? {
            val theme = IconTheme(context, themePackage)
            if (!theme.loadThemeResources()) {
                return null
            }

            val cmpMap = SimpleArrayMap<String, Int>(shortcuts.size)
            for (cellId in 0 until shortcuts.size) {
                val info = shortcuts.getOrDefault(cellId, null)
                if (info == null || info.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.isCustomIcon) {
                    continue
                }
                cmpMap.put(info.intent.component!!.className, cellId)
            }
            theme.loadFromXml(cmpMap)
            return theme
        }

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