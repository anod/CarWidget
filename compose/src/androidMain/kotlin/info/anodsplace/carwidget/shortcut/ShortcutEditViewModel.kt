package info.anodsplace.carwidget.shortcut

import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.shortcuts.ShortcutInfoFactory
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.shortcut.ShortcutEditViewAction.ShowToast
import info.anodsplace.carwidget.shortcut.intent.IntentField
import info.anodsplace.framework.content.ShowToastActionDefaults
import info.anodsplace.graphics.DrawableUri
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

@Immutable
data class ShortcutEditViewState(
    val shortcut: Shortcut? = null,
    val position: Int = -1,
    val shortcutId: Long = -1,
    val expanded: Boolean = false,
    val showIconPackPicker: Boolean = false,
    val iconVersion: Int = -1,
    val showFolderEditor: Boolean = false,
    val folderItems: ImmutableList<Shortcut> = persistentListOf(),
)

sealed interface ShortcutEditViewEvent {
    data class UpdateField(val field: IntentField) : ShortcutEditViewEvent
    data class ToggleAdvanced(val expanded: Boolean) : ShortcutEditViewEvent
    data object Drop : ShortcutEditViewEvent
    data class IconPackPicker(val show: Boolean) : ShortcutEditViewEvent
    data class IconPackResult(val intent: Intent?, val resolveProperties: DrawableUri.ResolveProperties) : ShortcutEditViewEvent
    data class CustomIconResult(val uri: Uri?, val resolveProperties: DrawableUri.ResolveProperties) : ShortcutEditViewEvent
    data class LaunchCustomizeError(val exception: Exception) : ShortcutEditViewEvent
    data object DefaultIconReset : ShortcutEditViewEvent
    data class ShowFolderEditor(val show: Boolean) : ShortcutEditViewEvent
    data class UpdateTitle(val title: String): ShortcutEditViewEvent
    data class UpdateFolderItems(val items: List<ShortcutIntent>): ShortcutEditViewEvent
}

sealed interface ShortcutEditViewAction {
    class ShowToast(text: String) : ShowToastActionDefaults(text = text), ShortcutEditViewAction
}

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
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShortcutEditViewModel(position, shortcutId, appWidgetIdScope) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope
    private val appScope: AppCoroutineScope by inject()
    private val shortcutsDatabase: ShortcutsDatabase by inject() //= org.koin.core.component.get()
    private val model: WidgetShortcutsModel by inject()
    private val context: Context by inject()
    val shortcutResources: ShortcutResources by inject()
    val widgetSettings: WidgetInterface by inject()
    val imageLoader: ImageLoader by inject()
    private var folderJob: Job? = null

    init {
        viewState = ShortcutEditViewState(
            position = position,
            shortcutId = shortcutId,
        )
        viewModelScope.launch {
            shortcutsDatabase.observeShortcut(shortcutId).collect { shortcut ->
                viewState = viewState.copy(shortcut = shortcut)
                if (shortcut?.isFolder == true) {
                    folderJob?.cancel()
                    folderJob = launch {
                        shortcutsDatabase.observeFolder(shortcutId).collect { items ->
                            viewState = viewState.copy(folderItems = items.toImmutableList())
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            shortcutsDatabase.observeIcon(shortcutId).collect { icon ->
                viewState = viewState.copy(iconVersion = icon.hashCode())
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
                viewState = viewState.copy(expanded = event.expanded,)
            }
            is ShortcutEditViewEvent.UpdateField -> {
                val shortcut = viewState.shortcut ?: return
                appScope.launch {
                    val intent = updateIntentField(event.field, shortcut.intent)
                    shortcutsDatabase.updateIntent(shortcut.id, intent)
                }
            }
            is ShortcutEditViewEvent.UpdateTitle -> {
                val shortcut = viewState.shortcut ?: return
                val newTitle = event.title
                // Optimistically update local state for immediate UI feedback
                viewState = viewState.copy(shortcut = shortcut.copy(title = newTitle),)
                appScope.launch {
                    shortcutsDatabase.updateTitle(shortcut.id, newTitle)
                }
            }
            is ShortcutEditViewEvent.IconPackPicker -> {
                viewState = viewState.copy(showIconPackPicker = event.show)
            }
            is ShortcutEditViewEvent.CustomIconResult -> {
                val shortcutIcon = iconFromUri(event.uri, hiRes = false, event.resolveProperties)
                if (shortcutIcon != null) {
                    updateIcon(shortcutIcon)
                }
            }
            is ShortcutEditViewEvent.IconPackResult -> {
                viewState = viewState.copy(showIconPackPicker = false)
                val shortcutIcon = iconFromIconPackResult(event.intent , event.resolveProperties)
                if (shortcutIcon != null) {
                    updateIcon(shortcutIcon)
                }
            }
            ShortcutEditViewEvent.DefaultIconReset -> {
                val shortcut = viewState.shortcut ?: return
                val component = shortcut.intent.component ?: return
                appScope.launch {
                    if (shortcut.isFolder) {
                        val iconResource = ShortcutIconResource.fromContext(context, shortcutResources.folderShortcutIcon)
                        val defaultIcon = ShortcutInfoFactory.resolveIconResource(viewState.shortcutId, iconResource, isCustom = false, context)
                            ?: ShortcutInfoFactory.defaultFallbackIcon(viewState.shortcutId, context)
                        shortcutsDatabase.updateIcon(shortcut.id, defaultIcon)
                        return@launch
                    }
                    val resolveInfo = context.packageManager.resolveActivity(shortcut.intent, 0)
                    val defaultIcon = ShortcutInfoFactory.resolveAppIcon(resolveInfo, component, context)
                    shortcutsDatabase.updateIcon(shortcut.id, defaultIcon)
                }
            }
            is ShortcutEditViewEvent.UpdateFolderItems -> {
                val shortcut = viewState.shortcut ?: return
                if (!shortcut.isFolder) return
                appScope.launch {
                    model.updateFolderItems(shortcut.id, event.items)
                    viewState = viewState.copy(showFolderEditor = false)
                }
            }
            is ShortcutEditViewEvent.ShowFolderEditor -> {
                viewState = viewState.copy(showFolderEditor = event.show)
            }
            is ShortcutEditViewEvent.LaunchCustomizeError -> {
                AppLog.e(event.exception)
                emitAction(ShowToast(text = "Cannot launch ${event.exception}"))
            }
        }
    }

    private fun updateIcon(icon: ShortcutIcon) {
        viewModelScope.launch {
            shortcutsDatabase.updateIcon(viewState.shortcutId, icon)
        }
    }

    private fun iconFromIconPackResult(intent: Intent?, resolveProperties: DrawableUri.ResolveProperties): ShortcutIcon? {
        if (intent == null) {
            return null
        }

        @Suppress("DEPRECATION")
        val iconResource = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE) as? ShortcutIconResource
        if (iconResource != null) {
            val icon = ShortcutInfoFactory.resolveIconResource(viewState.shortcutId, iconResource, isCustom = true, context)
            if (icon != null) {
                return icon
            }
        }

        if (intent.hasExtra("icon")) {
            val bitmap: Bitmap? = intent.getParcelableExtra("icon")
            if (bitmap != null) {
                val shortcutIcon = ShortcutIcon.forCustomIcon(viewState.shortcutId, bitmap)
                return shortcutIcon
            }
        }

        return iconFromUri(intent.data, hiRes = true, resolveProperties)
    }

    private fun iconFromUri(uri: Uri?, hiRes: Boolean, resolveProperties: DrawableUri.ResolveProperties): ShortcutIcon? {
        val imageUri = uri ?: return null
        val icon = DrawableUri(context).resolve(imageUri, resolveProperties) ?: return null
        val bitmap = if (hiRes) {
            UtilitiesBitmap.createHiResIconBitmap(icon, context)
        } else {
            UtilitiesBitmap.createMaxSizeIcon(icon, context)
        }
        return ShortcutIcon.forCustomIcon(viewState.shortcutId, bitmap)
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