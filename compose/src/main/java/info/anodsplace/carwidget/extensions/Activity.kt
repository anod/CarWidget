package info.anodsplace.carwidget.extensions

import android.app.Activity
import android.os.Bundle

fun Activity.applyTheme(newThemeIdx: Int) {
    // app.appComponent.theme = AppTheme(newThemeIdx)
//    AppCompatDelegate.setDefaultNightMode(app.nightMode)
//    requireActivity().setTheme(app.appComponent.theme.mainResource)
//    requireActivity().recreate()
}

fun Activity.extras(): Bundle = intent?.extras ?: Bundle.EMPTY