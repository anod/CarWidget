package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import org.koin.core.component.KoinComponent

class ShortcutEditViewModel(
        val position: Int,
        val shortcutId: Long,
        val appWidgetId: Int,
        application: Application
) : AndroidViewModel(application), KoinComponent {

    class Factory(
            private val position: Int,
            private val shortcutId: Long,
            private val appWidgetId: Int,
            private val application: Application
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ShortcutEditViewModel(position, shortcutId, appWidgetId, application) as T
        }
    }

    private val context: Context
        get() = getApplication()

    val model = WidgetShortcutsModel.init(context, DefaultsResourceProvider(context), appWidgetId)
    val shortcut = model.shortcutsDatabase.loadShortcut(shortcutId)!!
    val icon = model.iconLoader.load(shortcut)
}