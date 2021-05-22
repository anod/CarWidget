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
fun AboutButton(@StringRes titleRes: Int,
                subtitle: String,
                onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colors.onSurface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = titleRes).toUpperCase(Locale.getDefault()))
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
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AboutTitle(titleRes = R.string.app_preferences)
        AboutButton(titleRes = R.string.app_theme, "Dark", onClick = {})
        AboutButton(titleRes = R.string.music_app, "SHOW CHOICE", onClick = {})
        AboutButton(titleRes = R.string.default_car_dock_app, "", onClick = {})
        AboutTitle(titleRes = R.string.pref_backup_title)
        AboutButton(titleRes = R.string.backup_current_widget, "", onClick = {})
        AboutButton(titleRes = R.string.backup_incar_settings, "", onClick = {})
        AboutButton(titleRes = R.string.restore, "", onClick = {})
        AboutTitle(titleRes = R.string.information_title)
        AboutButton(titleRes = R.string.version_summary, "RATE", onClick = {})
    }
}

@Preview("About Screen Light")
@Composable
fun PreviewAboutScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            AboutScreen()
        }
    }
}

@Preview("About Screen Dark")
@Composable
fun PreviewAboutScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            AboutScreen()
        }
    }
}