package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.screens.shortcuts.intent.IntentField
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

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
        val shortcutId: Long,
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

    private val model = WidgetShortcutsModel.init(context, get(), DefaultsResourceProvider(context), appWidgetId)
    val shortcut = model.shortcutsDatabase.observeShortcut(shortcutId).stateIn(
            viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = null
    )
    val icon = shortcut.filterNotNull().map { sh -> model.iconLoader.load(sh) }

    override fun drop() {
        model.drop(position)
    }

    override fun updateField(field: IntentField) {
        val shortcut = shortcut.value ?: return
        val intent = shortcut.intent
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

        model.shortcutsDatabase.updateIntent(shortcut.id, intent)
    }
}