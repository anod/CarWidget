package info.anodsplace.carwidget.compose

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R

@Composable
fun ButtonsPanel(onDismissRequest: () -> Unit) {
    Column {
        Divider()
        Row(
                Modifier
                        .preferredHeight(56.dp)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Button(onClick = onDismissRequest, modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Button(onClick = onDismissRequest, modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(text = stringResource(id = R.string.save))
            }
        }
        Spacer(modifier = Modifier.preferredHeight(8.dp))
    }
}