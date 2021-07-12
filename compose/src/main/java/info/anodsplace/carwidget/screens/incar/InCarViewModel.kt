package info.anodsplace.carwidget.screens.incar

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.carwidget.content.model.NotificationShortcutsModel
import info.anodsplace.carwidget.content.preferences.InCarInterface
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InCarViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val context: Context
        get() = getApplication()

    val inCar: InCarInterface by inject()
    val items = createCarScreenItems(inCar)
    val notificationShortcuts = NotificationShortcutsModel.init(context)
}