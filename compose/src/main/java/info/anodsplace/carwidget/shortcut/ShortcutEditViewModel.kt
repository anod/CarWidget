package info.anodsplace.carwidget.shortcut

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.shortcut.intent.IntentField
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.scope.Scope

data class ShortcutEditViewState(
    val shortcut: Shortcut? = null,
    val position: Int = -1,
    val shortcutId: Long = -1,
    val expanded: Boolean = false
)

sealed interface ShortcutEditViewEvent {
    class UpdateField(val field: IntentField) : ShortcutEditViewEvent
    class ToggleAdvanced(val expanded: Boolean) : ShortcutEditViewEvent
    object Drop : ShortcutEditViewEvent
}

sealed interface ShortcutEditViewAction

class ShortcutEditViewModel(
        position: Int,
        shortcutId: Long,
        appWidgetIdScope: AppWidgetIdScope,
) : BaseFlowViewModel<ShortcutEditViewState, ShortcutEditViewEvent, ShortcutEditViewAction>(), KoinScopeComponent {

    class Factory(
            private val position: Int,
            private val shortcutId: Long,
            private val appWidgetIdScope: AppWidgetIdScope
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShortcutEditViewModel(position, shortcutId, appWidgetIdScope) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope
    private val appScope: AppCoroutineScope by inject()
    private val shortcutsDatabase: ShortcutsDatabase = get()
    private val model: WidgetShortcutsModel by inject()
    val widgetSettings: WidgetInterface by inject()
    val imageLoader: ImageLoader by inject()

    init {
        viewState = ShortcutEditViewState(
            position = position,
            shortcutId = shortcutId,
        )
        viewModelScope.launch {
            shortcutsDatabase.observeShortcut(shortcutId).collect { shortcut ->
                viewState = viewState.copy(shortcut = shortcut)
            }
        }
    }

    override fun handleEvent(event: ShortcutEditViewEvent) {
        when (event) {
            ShortcutEditViewEvent.Drop -> {
                appScope.launch {
                    model.drop(viewState.position)
                }
            }
            is ShortcutEditViewEvent.ToggleAdvanced -> {
                viewState = viewState.copy(expanded = event.expanded)
            }
            is ShortcutEditViewEvent.UpdateField -> {
                val shortcut = viewState.shortcut ?: return
                appScope.launch {
                    val intent = updateIntentField(event.field, shortcut.intent)
                    shortcutsDatabase.updateIntent(shortcut.id, intent)
                }
            }
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