package info.anodsplace.carwidget.screens.widget

import android.app.Application
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

class EditWidgetViewModel(
    application: Application,
    appWidgetIdScope: AppWidgetIdScope,
    private val buttonId: Any?
) : AndroidViewModel(application), KoinScopeComponent {

    class Factory(private val appContext: Context, private val appWidgetIdScope: AppWidgetIdScope, private val buttonId: Int): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T
            = EditWidgetViewModel(appContext as Application, appWidgetIdScope, buttonId) as T
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val widgetSettings: WidgetInterface by inject()
    private val skinProperties: SkinProperties by inject(parameters = { parametersOf(widgetSettings.skin) })
    val imageLoader: ImageLoader by inject()

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
        val newValue = item.extras!!.getInt("button")
        if (buttonId == WidgetInterface.BUTTON_ID_1) {
            widgetSettings.widgetButton1 = newValue
        } else {
            widgetSettings.widgetButton2 = newValue
        }
        widgetSettings.applyPending()
    }

}