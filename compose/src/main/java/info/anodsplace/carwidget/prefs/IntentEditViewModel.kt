package info.anodsplace.carwidget.prefs

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import info.anodsplace.carwidget.compose.UiAction
import info.anodsplace.framework.livedata.SingleLiveEvent

class IntentEditViewModel(application: Application): AndroidViewModel(application) {

    val extras = MutableLiveData<Bundle>()
    val action = SingleLiveEvent<UiAction>()

}