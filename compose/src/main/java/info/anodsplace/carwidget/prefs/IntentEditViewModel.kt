package info.anodsplace.carwidget.prefs

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import info.anodsplace.carwidget.compose.UiAction
import info.anodsplace.framework.livedata.SingleLiveEvent

class IntentEditViewModel(application: Application): AndroidViewModel(application) {
    val intent = MutableLiveData(Intent())
    val action = SingleLiveEvent<UiAction>()

    fun updateField(field: IntentField) {
        val intent = intent.value!!
        when (field) {
            is IntentField.Action -> {
                intent.action = field.value
            }
            is IntentField.Data -> {
                intent.data = field.uri
            }
        }
    }
}