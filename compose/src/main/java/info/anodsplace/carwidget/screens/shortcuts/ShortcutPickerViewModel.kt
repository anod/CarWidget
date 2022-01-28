package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.appwidget.WidgetUpdate
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

sealed class ShortcutPickerAction {
    data class SavedSuccess(val resultCode: Int): ShortcutPickerAction()
    object SavedError: ShortcutPickerAction()

}

class ShortcutPickerViewModel(
    val position: Int,
    val appWidgetId: Int,
    application: Application
) : AndroidViewModel(application), KoinComponent {
    private val update: WidgetUpdate by inject()
    val shortcutResources: ShortcutResources by inject()
    val actions = MutableSharedFlow<ShortcutPickerAction>()

    class Factory(
        private val position: Int,
        private val appWidgetId: Int,
        private val application: Application
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShortcutPickerViewModel(position, appWidgetId, application) as T
        }
    }

    private val context: Context
        get() = getApplication()

    private val model = WidgetShortcutsModel(context, get(), DefaultsResourceProvider(context), appWidgetId)

    suspend fun save(intent: Intent, isApplicationShortcut: Boolean): Int = withContext(Dispatchers.IO) {
        val result = model.saveIntent(position, intent, isApplicationShortcut)
        update.request(intArrayOf(appWidgetId))
        return@withContext result.second
    }
}