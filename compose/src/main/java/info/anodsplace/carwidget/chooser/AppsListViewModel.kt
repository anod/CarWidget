package info.anodsplace.carwidget.chooser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow

class AppsListViewModel(application: Application, private val loader: ChooserLoader) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val loader: ChooserLoader
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppsListViewModel(application, loader) as T
        }
    }

    fun load(): Flow<List<ChooserEntry>> = loader.load()
}