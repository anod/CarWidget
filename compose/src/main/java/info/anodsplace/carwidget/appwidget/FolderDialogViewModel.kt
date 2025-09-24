package info.anodsplace.carwidget.appwidget

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

@Immutable
data class FolderDialogViewState(
    val position: Int,
    val items: List<Shortcut>,
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
        viewState = FolderDialogViewState(
            position = position,
            items = emptyList(),
            title = extras.getString(Intent.EXTRA_SHORTCUT_NAME) ?: ""
        )
        viewModelScope.launch {
            val shortcut = db.loadShortcut(targetId = +appWidgetIdScope, position = position)
            if (shortcut != null) {
                val items = db.loadFolderItems(shortcut.id)
                viewState = viewState.copy(title = shortcut.title.toString(), items = items)
            }
        }
    }

    override fun handleEvent(event: Unit) {

    }
}