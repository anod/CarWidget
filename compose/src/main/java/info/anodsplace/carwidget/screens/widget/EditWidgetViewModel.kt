package info.anodsplace.carwidget.screens.widget

import android.app.Application
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class EditWidgetViewModel(application: Application, private val appWidgetId: Int, private val buttonId: Any?) : AndroidViewModel(application), KoinComponent {
    class Factory(private val appContext: Context, private val appWidgetId: Int, private val buttonId: Int): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EditWidgetViewModel(appContext as Application, appWidgetId, buttonId) as T
    }

    private val widgetSettings: WidgetInterface by inject(parameters = { parametersOf(appWidgetId) })
    private val skinProperties: SkinProperties by inject(parameters = { parametersOf(widgetSettings.skin) })

    val items = listOf(
        ChooserEntry(
            componentName = null,
            title = application.getString(R.string.pref_settings_transparent),
            intent = null,
            iconRes = skinProperties.settingsButtonRes,
            extras = bundleOf("button" to WidgetInterface.WIDGET_BUTTON_SETTINGS)
        ),
        ChooserEntry(
            componentName = null,
            title = application.getString(R.string.pref_incar_transparent),
            intent = null,
            iconRes = skinProperties.inCarButtonEnterRes,
            extras = bundleOf("button" to WidgetInterface.WIDGET_BUTTON_INCAR)
        ),
        ChooserEntry(
            componentName = null,
            title = application.getString(R.string.hidden),
            intent = null,
            iconRes = 0,
            extras = bundleOf("button" to WidgetInterface.WIDGET_BUTTON_HIDDEN)
        )
    )

    fun onSelect(item: ChooserEntry) {
        if (buttonId == WidgetInterface.BUTTON_ID_1) {
            widgetSettings.widgetButton1 = item.extras!!.getInt("button")
        } else {
            widgetSettings.widgetButton2 = item.extras!!.getInt("button")
        }
        widgetSettings.applyPending()
    }
}