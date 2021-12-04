package info.anodsplace.carwidget.screens.incar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.content.forLauncher

private fun autorunItem(context: Context, componentName: ComponentName?, listItem: PreferenceItem.List): PreferenceItem.List {
    return if (componentName == null) {
        listItem.copy(summaryRes = R.string.disabled, summary = "")
    } else {
        val resolveInfo = context.packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).apply {
            component = componentName
        }, 0)
        val appTitle = if (resolveInfo != null) {
            resolveInfo.activityInfo.loadLabel(context.packageManager) as String
        } else componentName.packageName
        listItem.copy(summaryRes = 0, summary = appTitle)
    }
}

private fun value(componentName: ComponentName?): String {
    return if (componentName == null) "disabled" else "custom"
}

private fun createItems(inCar: InCarInterface, context: Context) = listOf(
    PreferenceItem.CheckBox(
        checked = inCar.isActivateCarMode,
        titleRes = R.string.pref_activate_car_mode,
        summaryRes = R.string.pref_activate_car_mode_summary,
        key = "activate-car-mode"),
    autorunItem(context, inCar.autorunApp, listItem = PreferenceItem.List(
        titleRes = R.string.pref_autorun_app_title,
        entries = R.array.autorun_app_titles,
        entryValues = R.array.autorun_app_values,
        value = value(inCar.autorunApp),
        key = "autorun-app-choose"
    ))
)

@Composable
fun MoreScreen(inCar: InCarInterface, modifier: Modifier) {
    val context = LocalContext.current
    var appChooser: PreferenceItem.List? by remember { mutableStateOf(null) }
    var items: List<PreferenceItem> by remember { mutableStateOf(createItems(inCar, context)) }

    PreferencesScreen(
        modifier = modifier.fillMaxSize(),
        preferences = items,
        categoryColor = MaterialTheme.colors.secondary,
        descriptionColor = MaterialTheme.colors.onBackground,
        onClick = { item ->
        when (item) {
            is PreferenceItem.CheckBox -> {
                inCar.applyChange(item.key, item.checked)
            }
            is PreferenceItem.List -> {
                if (item.value == "disabled") {
                    inCar.autorunApp = null
                    items = createItems(inCar, context)
                } else {
                    appChooser = item
                }
            }
            else -> { }
        }
    })

    if (appChooser != null) {
        val loader by remember { mutableStateOf(QueryIntentLoader(context, Intent().forLauncher())) }

        ChooserDialog(
                modifier = Modifier.fillMaxHeight(fraction = 0.8f),
                loader = loader,
                onDismissRequest = {
            items = createItems(inCar, context)
            appChooser = null
        }) {
            inCar.autorunApp = it.componentName
            items = createItems(inCar, context)
            appChooser = null
        }
    }
}