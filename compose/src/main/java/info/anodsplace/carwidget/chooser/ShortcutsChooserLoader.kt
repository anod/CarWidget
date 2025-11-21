package info.anodsplace.carwidget.chooser

import android.content.Context
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.compose.chooser.ChooserEntry
import info.anodsplace.compose.chooser.ChooserLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.Collator
import java.util.Locale

/**
 * ChooserLoader backed by a static list of Intents. Each intent's component/activity label
 * is resolved into a ChooserEntry. Intents without a resolvable component are skipped.
 */
class ShortcutsChooserLoader(
    private val context: Context,
    private val shortcuts: List<Shortcut>
) : ChooserLoader {
    private val collator: Collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }

    override fun load(): Flow<List<ChooserEntry>> = flowOf(
        shortcuts
            .mapNotNull { toEntry(it) }
            .sortedWith { a, b -> collator.compare(a.title, b.title) }
    )

    private fun toEntry(shortcut: Shortcut): ChooserEntry? {
        val cmp = shortcut.intent.component ?: return null
        return ChooserEntry(
            componentName = cmp,
            title = shortcut.title.toString(),
            intent = shortcut.intent
        )
    }
}
