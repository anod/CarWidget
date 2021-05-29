package info.anodsplace.carwidget.screens.incar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface

@Composable
fun NotificationShortcuts(inCar: InCarInterface, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(4f)
        ) {
            Text(
                text = stringResource(id = R.string.shortcuts),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body2
            )
            Text(
                text = stringResource(id = R.string.shortcuts_summary),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body2
            )
        }
        Icon(
            modifier = Modifier
                .weight(1f)
                .size(56.dp)
                .padding(4.dp),
            imageVector = Icons.Filled.Add,
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .weight(1f)
                .size(56.dp)
                .padding(4.dp),
            imageVector = Icons.Filled.Add,
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .weight(1f)
                .size(56.dp)
                .padding(4.dp),
            imageVector = Icons.Filled.Add,
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
    }
}

@Preview("ShortcutsScreen Dark")
@Composable
fun ShortcutsScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            NotificationShortcuts(
                inCar = InCarInterface.NoOp(),
                modifier = Modifier
            )
        }
    }
}