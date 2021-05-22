package info.anodsplace.carwidget.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import java.util.*

@Composable
fun AboutButton(@StringRes titleRes: Int = 0,
                title: String = "",
                subtitle: String = "",
                onClick: () -> Unit = {},
                enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colors.onSurface
        ),
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (titleRes != 0) {
                stringResource(id = titleRes).toUpperCase(Locale.getDefault())
            } else title.toUpperCase(Locale.getDefault()))
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle.toUpperCase(Locale.getDefault()),
                    style = MaterialTheme.typography.caption.copy(
                        fontSize = 8.sp
                    ))
            }
        }
    }
}

@Composable
fun AboutTitle(@StringRes titleRes: Int) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        text = stringResource(id = titleRes).toUpperCase(Locale.getDefault()),
        style = MaterialTheme.typography.overline
    )
}

@Composable
fun AboutScreen(screenState: AboutScreenState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AboutTitle(titleRes = R.string.app_preferences)
        AboutButton(titleRes = R.string.app_theme, subtitle = screenState.themeName, onClick = {})
        AboutButton(titleRes = R.string.music_app, subtitle = screenState.musicApp, onClick = {})
        AboutButton(titleRes = R.string.default_car_dock_app, onClick = {})
        AboutTitle(titleRes = R.string.pref_backup_title)
        AboutButton(titleRes = R.string.backup_current_widget, enabled = screenState.isValidWidget, onClick = {})
        AboutButton(titleRes = R.string.backup_incar_settings, onClick = {})
        AboutButton(titleRes = R.string.restore, onClick = {})
        AboutTitle(titleRes = R.string.information_title)
        AboutButton(title = screenState.appVersion, subtitle = stringResource(id = R.string.version_summary), onClick = {})
    }
}

@Preview("About Screen Light")
@Composable
fun PreviewAboutScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            AboutScreen(AboutScreenState(0, 0, "Light", "CHOICE", "DUMMY"))
        }
    }
}

@Preview("About Screen Dark")
@Composable
fun PreviewAboutScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            AboutScreen(AboutScreenState(1, 0, "Dark", "Yandex", "v123"))
        }
    }
}