package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.screens.shortcuts.intent.IntentField
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

interface ShortcutEditDelegate {
    fun drop()
    fun updateField(field: IntentField)

    class NoOp: ShortcutEditDelegate {
        override fun drop() { }
        override fun updateField(field: IntentField) { }
    }
}

class ShortcutEditViewModel(
        val position: Int,
        shortcutId: Long,
        val appWidgetId: Int,
        application: Application
) : AndroidViewModel(application), KoinComponent, ShortcutEditDelegate {

    class Factory(
            private val position: Int,
            private val shortcutId: Long,
            private val appWidgetId: Int,
            private val application: Application
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShortcutEditViewModel(position, shortcutId, appWidgetId, application) as T
        }
    }

    private val context: Context
        get() = getApplication()

    private val appScope: AppCoroutineScope by inject()
    private val shortcutsDatabase: ShortcutsDatabase = get()
    private val iconLoader: ShortcutIconLoader = get()
    private val widgetSettings: WidgetInterface by inject(parameters = { parametersOf(appWidgetId) })

    private val model = WidgetShortcutsModel(context, shortcutsDatabase, DefaultsResourceProvider(context), appWidgetId)
    val shortcut = shortcutsDatabase.observeShortcut(shortcutId).stateIn(
            viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = null
    )
    val icon = shortcut.filterNotNull().map { sh -> iconLoader.load(sh, widgetSettings.adaptiveIconPath) }

    override fun drop() {
        appScope.launch {
            model.drop(position)
        }
    }

    override fun updateField(field: IntentField) {
        val shortcut = shortcut.value ?: return
        appScope.launch {
            val intent = updateIntentField(field, shortcut.intent)
            shortcutsDatabase.updateIntent(shortcut.id, intent)
        }
    }

    private fun updateIntentField(field: IntentField, intent: Intent): Intent {
        when (field) {
            is IntentField.Action -> {
                intent.action = field.value
            }
            is IntentField.Data -> {
                intent.data = field.uri
            }
            is IntentField.Categories -> {
                val existing = intent.categories ?: emptySet()
                val new = field.value ?: emptySet()
                (existing - new).forEach { intent.removeCategory(it) }
                new.forEach {
                    if (!intent.hasCategory(it)) {
                        intent.addCategory(it)
                    }
                }
            }
            is IntentField.Component -> {
                val comp = field.value
                if (comp == null || (comp.packageName.isBlank() && comp.className.isBlank())) {
                    intent.component = null
                } else if (comp.packageName.isNotBlank() || comp.className.isNotBlank()) {
                    intent.component = comp
                }
            }
            is IntentField.Extras -> {
                intent.putExtras(field.bundle)
            }
            is IntentField.Flags -> {
                intent.flags = field.value
            }
            is IntentField.MimeType -> {
                intent.type = field.value
            }
        }

        return intent
    }
}