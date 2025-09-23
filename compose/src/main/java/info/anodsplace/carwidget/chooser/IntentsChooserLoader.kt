package info.anodsplace.carwidget.chooser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.Collator
import java.util.Locale

/**
 * ChooserLoader backed by a static list of Intents. Each intent's component/activity label
 * is resolved into a ChooserEntry. Intents without a resolvable component are skipped.
 */
class IntentsChooserLoader(
    private val context: Context,
    private val intents: List<Intent>
) : ChooserLoader {
    private val pm: PackageManager = context.packageManager
    private val collator: Collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }

    override fun load(): Flow<List<ChooserEntry>> = flowOf(
        intents
            .mapNotNull { toEntry(it) }
            .sortedWith { a, b -> collator.compare(a.title, b.title) }
    )

    private fun toEntry(intent: Intent): ChooserEntry? {
        val cmp = intent.component ?: return null
        val label = try {
            pm.getActivityInfo(cmp, 0).loadLabel(pm).toString()
        } catch (_: Exception) {
            cmp.className.substringAfterLast('.')
        }
        return ChooserEntry(componentName = cmp, title = label, intent = intent)
    }
}
