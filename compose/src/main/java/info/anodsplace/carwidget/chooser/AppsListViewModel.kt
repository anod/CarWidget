package info.anodsplace.carwidget.chooser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AppsListViewModel(application: Application, private val loader: AppsListLoader) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val loader: AppsListLoader
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AppsListViewModel(application, loader) as T
        }
    }

    fun load(): Flow<List<ChooserEntry>> = loader.load()
}