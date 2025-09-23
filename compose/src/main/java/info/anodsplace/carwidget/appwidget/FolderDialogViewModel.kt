package info.anodsplace.carwidget.appwidget

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra.EXTRA_FOLDER_ITEM_URIS_JSON
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

@Immutable
data class FolderDialogViewState(
    val position: Int,
    val intents: List<Intent>,
    val title: String
)

class FolderDialogViewModel(
    application: Application,
    appWidgetIdScope: AppWidgetIdScope,
    position: Int,
    extras: Bundle
) : BaseFlowViewModel<FolderDialogViewState, Unit, Unit>(), KoinScopeComponent {

    class Factory(
        private val appContext: Context,
        private val appWidgetIdScope: AppWidgetIdScope,
        private val position: Int,
        private val extras: Bundle
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T
                = FolderDialogViewModel(appContext as Application, appWidgetIdScope, position, extras) as T
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val db: ShortcutsDatabase by inject()

    init {
        val intents: List<Intent> = when {
            extras.containsKey(EXTRA_FOLDER_ITEM_URIS_JSON) -> {
                val json = extras.getString(EXTRA_FOLDER_ITEM_URIS_JSON) ?: "[]"
                try {
                    val arr = JSONArray(json)
                    (0 until arr.length()).mapNotNull { idx ->
                        val s = arr.optString(idx, null) ?: return@mapNotNull null
                        try { Intent.parseUri(s, 0) } catch (_: Exception) { null }
                    }
                } catch (_: Exception) { emptyList() }
            }
            else -> emptyList()
        }
        viewState = FolderDialogViewState(
            position = position,
            intents = intents,
            title = extras.getString(Intent.EXTRA_SHORTCUT_NAME) ?: ""
        )
        viewModelScope.launch {
            db.loadShortcut(targetId = +appWidgetIdScope, position = position)?.let { shortcut ->
                viewState = viewState.copy(title = shortcut.title.toString())
            }
        }
    }

    override fun handleEvent(event: Unit) {

    }
}