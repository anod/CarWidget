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
    val editField: MutableLiveData<IntentField?> = MutableLiveData()
}