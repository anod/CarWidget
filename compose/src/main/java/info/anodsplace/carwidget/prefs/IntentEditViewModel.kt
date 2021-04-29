package info.anodsplace.carwidget.prefs

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.carwidget.compose.UiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class IntentEditViewModel(application: Application): AndroidViewModel(application) {
    val intent = MutableStateFlow(Intent())
    val action = MutableSharedFlow<UiAction>()

    fun updateField(field: IntentField) {
        val current = intent.value
        when (field) {
            is IntentField.Action -> {
                current.action = field.value
            }
            is IntentField.Data -> {
                current.data = field.uri
            }
            is IntentField.Categories -> {
                val existing = current.categories ?: emptySet()
                val new = field.value ?: emptySet()
                (existing - new).forEach { current.removeCategory(it) }
                new.forEach {
                    if (!current.hasCategory(it)) {
                        current.addCategory(it)
                    }
                }
            }
            is IntentField.Component -> {
                val comp = field.value
                if (comp == null || (comp.packageName.isBlank() && comp.className.isBlank())) {
                    current.component = null
                } else if (comp.packageName.isNotBlank() || comp.className.isNotBlank()) {
                    current.component = comp
                }
            }
            is IntentField.Extras -> {
                current.putExtras(field.bundle)
            }
            is IntentField.Flags -> {
                current.flags = field.value
            }
            is IntentField.MimeType -> {
                current.type = field.value
            }
        }
        intent.value = current
    }
}