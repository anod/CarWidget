package info.anodsplace.carwidget.screens

import androidx.compose.runtime.Composable
import info.anodsplace.carwidget.compose.PreferencesScreen
import info.anodsplace.carwidget.preferences.createCarScreenItems

@Composable
fun InCarScreen(inCar: info.anodsplace.carwidget.content.preferences.InCarInterface) {
    PreferencesScreen(preferences = createCarScreenItems(inCar), onClick = {})
}