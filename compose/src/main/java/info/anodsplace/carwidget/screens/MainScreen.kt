package info.anodsplace.carwidget.screens

import android.appwidget.AppWidgetManager
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface

@Composable
fun MainScreen(
    inCar: InCarInterface,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    initialNavigation: Int = 0
) {
    val (selectedNavigation, navigateTo) = remember { mutableStateOf(initialNavigation) }
    val items = listOf(
        stringResource(id = R.string.widgets),
        stringResource(id = R.string.pref_incar_mode_title),
        stringResource(id = R.string.info),
    )
    val icons = listOf(
        Icons.Filled.Widgets,
        Icons.Filled.DirectionsCar,
        Icons.Outlined.Info
    )

    Scaffold(
        topBar = {
            TopAppBar {
                Text(text = stringResource(id = R.string.app_name))
            }
        },
        bottomBar = {
            BottomNavigation {
                items.forEachIndexed { index, item ->
                    BottomNavigationItem(
                        icon = { Icon(icons[index], contentDescription = null ) },
                        label = { Text(item) },
                        selected = selectedNavigation == index,
                        onClick = { navigateTo(index) }
                    )
                }
            }
        }
    ) {
        when (selectedNavigation) {
            0 -> Text(items[selectedNavigation])
            1 -> InCarScreen(inCar)
            2 -> {
                val aboutViewModel: AboutViewModel = viewModel()
                val aboutScreenState by aboutViewModel.screenState.collectAsState()
                if (aboutScreenState == null) {
                    aboutViewModel.init(appWidgetId = appWidgetId)
                } else {
                    AboutScreen(aboutScreenState!!)
                }
            }
        }
    }
}

@Preview("Main Screen Light")
@Composable
fun PreviewPreferencesScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), initialNavigation = 2)
        }
    }
}

@Preview("Main Screen Dark")
@Composable
fun PreviewPreferencesScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), initialNavigation = 1)
        }
    }
}