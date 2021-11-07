package info.anodsplace.carwidget.screens.incar

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.carwidget.chooser.ChooserLoader
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.content.shortcuts.NotificationShortcutsModel
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.content.forLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InCarViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    val appsLoader: ChooserLoader
        get() = QueryIntentLoader(context, Intent().forLauncher())
    private val context: Context
        get() = getApplication()

    val inCar: InCarInterface by inject()
    val items = createCarScreenItems(inCar)
    val notificationShortcuts = NotificationShortcutsModel.init(context)
}