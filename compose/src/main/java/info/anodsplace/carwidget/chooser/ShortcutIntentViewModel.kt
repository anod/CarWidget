package info.anodsplace.carwidget.chooser

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.AndroidViewModel
import org.koin.core.component.KoinComponent
import java.util.*

class ShortcutIntentViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val context: Context
        get() = getApplication()
    private val packageManager: PackageManager
        get() = context.packageManager

    private fun resolveIntentItems(baseIntent: Intent) {
        val list = packageManager.queryIntentActivities(
            baseIntent, 0 /* no flags */
        )
        Collections.sort(list, ResolveInfo.DisplayNameComparator(packageManager))

        val listSize = list.size
        for (i in 0 until listSize) {
            val resolveInfo = list[i]
            items.add(PickAdapter.Item(this, packageManager, resolveInfo))
        }
    }
}