package info.anodsplace.carwidget.chooser

import android.content.Context
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.extentions.isDebugBuild
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.compose.chooser.ChooserEntry
import info.anodsplace.compose.chooser.ChooserLoader
import info.anodsplace.compose.chooser.sectionEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

/**
 * Loader that aggregates shortcuts from all widgets. Current widget shortcuts appear first.
 */
class AllWidgetShortcutsChooserLoader(
    private val context: Context,
    private val currentWidgetId: Int,
): ChooserLoader, KoinComponent {
    val widgetIds: WidgetIds by inject()
    val db: ShortcutsDatabase by inject()

    companion object {
        fun sectionForWidget(widgetId: Int, context: Context) = sectionEntry(
            sectionId = "section-${widgetId}",
            title = context.getString(R.string.widget_header, widgetId)
        )
    }

    override fun load(): Flow<List<ChooserEntry>> = flow {
        emit(loadAll())
    }

    private suspend fun loadAll(): List<ChooserEntry> = withContext(Dispatchers.IO) {
        val allWidgetOrder = (listOf(currentWidgetId) + widgetIds.getLargeWidgetIds().filter { it != currentWidgetId }).distinct()
        val result = mutableListOf<ChooserEntry>()
        for (appWidgetId in allWidgetOrder) {
            AppWidgetIdScope(appWidgetId, instance = Random.nextInt(), existingScope = null).use {
                val widgetSettings = it.scope.get<WidgetSettings>()
                result.add(sectionForWidget(widgetId = appWidgetId, context = context))
                val shortcutsMap = db.loadTarget(appWidgetId)
                val shortcuts = shortcutsMap.values.filterNotNull()
                    .sortedWith { a, b -> a.position.compareTo(b.position) }
                shortcuts.forEach { shortcut ->
                    result.add(toEntry(shortcut, widgetSettings))
                }
            }
        }
        return@withContext result
    }

    private fun toEntry(shortcut: Shortcut, widgetSettings: WidgetSettings): ChooserEntry {
        return ChooserEntry(
            componentName = shortcut.intent.component,
            title = shortcut.title.toString(),
            intent = shortcut.intent,
            iconUri = shortcut.iconUri(
                isDebug = context.isDebugBuild,
                adaptiveIconStyle = widgetSettings.adaptiveIconStyle,
                skinName = widgetSettings.skin
            )
        ).apply {
            sourceShortcutId = shortcut.id
        }
    }

}

