package info.anodsplace.carwidget.chooser

import android.content.Context
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.compose.chooser.ChooserEntry
import info.anodsplace.compose.chooser.ChooserLoader
import info.anodsplace.compose.chooser.sectionEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.Collator
import java.util.Locale

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

    private val collator: Collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }

    override fun load(): Flow<List<ChooserEntry>> = flow {
        emit(loadAll())
    }

    private suspend fun loadAll(): List<ChooserEntry> = withContext(Dispatchers.IO) {
        val allWidgetOrder = (listOf(currentWidgetId) + widgetIds.getLargeWidgetIds().filter { it != currentWidgetId }).distinct()
        val result = mutableListOf<ChooserEntry>()
        for (wid in allWidgetOrder) {
            result.add(sectionForWidget(widgetId = wid, context = context))
            val shortcutsMap = db.loadTarget(wid)
            val shortcuts = shortcutsMap.values.filterNotNull().sortedWith { a, b -> a.position.compareTo(b.position) }
            shortcuts.forEach { shortcut ->
                result.add(toEntry(shortcut))
            }
        }
        return@withContext result
    }

    private fun toEntry(shortcut: Shortcut): ChooserEntry {
        return ChooserEntry(
            componentName = shortcut.intent.component,
            title = shortcut.title.toString(),
            intent = shortcut.intent,
        ).apply {
            sourceShortcutId = shortcut.id
        }
    }
}

