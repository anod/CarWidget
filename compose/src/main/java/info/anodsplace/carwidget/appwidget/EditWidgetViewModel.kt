package info.anodsplace.carwidget.appwidget

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.viewmodel.BaseFlowViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

@Immutable
data class EditWidgetViewState(
    val buttonId: Any?,
    val items: List<ChooserEntry>
)

sealed interface EditWidgetViewEvent {
    class Select(val entry: ChooserEntry): EditWidgetViewEvent
}

sealed interface EditWidgetViewAction

class EditWidgetViewModel(
    application: Application,
    appWidgetIdScope: AppWidgetIdScope,
    buttonId: Any?
) : BaseFlowViewModel<EditWidgetViewState, EditWidgetViewEvent, EditWidgetViewAction>(), KoinScopeComponent {

    class Factory(private val appContext: Context, private val appWidgetIdScope: AppWidgetIdScope, private val buttonId: Int): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T
            = EditWidgetViewModel(appContext as Application, appWidgetIdScope, buttonId) as T
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val widgetSettings: WidgetInterface by inject()
    private val skinProperties: SkinProperties by inject(parameters = { parametersOf(widgetSettings.skin) })
    val imageLoader: ImageLoader by inject()

    init {
        viewState = EditWidgetViewState(
            buttonId = buttonId,
            items = listOf(
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
                    iconRes = skinProperties.buttonAlternativeHiddenResId,
                    extras = bundleOf("button" to WidgetInterface.WIDGET_BUTTON_HIDDEN)
                )
            )
        )
    }

    override fun handleEvent(event: EditWidgetViewEvent) {
        when (event) {
            is EditWidgetViewEvent.Select -> onSelect(event.entry)
        }
    }

    private fun onSelect(item: ChooserEntry) {
        val newValue = item.extras!!.getInt("button")
        if (viewState.buttonId == WidgetInterface.BUTTON_ID_1) {
            widgetSettings.widgetButton1 = newValue
        } else {
            widgetSettings.widgetButton2 = newValue
        }
        widgetSettings.applyPending()
    }
}