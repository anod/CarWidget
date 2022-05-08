package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.WidgetUpdate
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.shortcuts.CreateShortcutResult
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

sealed class ShortcutPickerAction {
    data class SavedSuccess(val resultCode: Int): ShortcutPickerAction()
    object SavedError: ShortcutPickerAction()

}

class ShortcutPickerViewModel(
    val position: Int,
    val appWidgetIdScope: AppWidgetIdScope,
    application: Application
) : AndroidViewModel(application), KoinScopeComponent {

    class Factory(
        private val position: Int,
        private val appWidgetIdScope: AppWidgetIdScope,
        private val application: Application
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShortcutPickerViewModel(position, appWidgetIdScope, application) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val appScope: AppCoroutineScope by inject()
    private val update: WidgetUpdate by inject()
    private val model by inject<WidgetShortcutsModel>()
    val shortcutResources: ShortcutResources by inject()
    val actions = MutableSharedFlow<ShortcutPickerAction>()
    val saveResult = MutableSharedFlow<CreateShortcutResult>()

    fun save(intent: Intent, isApp: Boolean) {
        appScope.launch {
            try {
                val result = model.saveIntent(position, intent, isApp)
                update.request(intArrayOf(+appWidgetIdScope))
                saveResult.emit(result.second)
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }
    }
}