package info.anodsplace.carwidget.compose

import android.content.Intent
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview

@Composable
fun IntentEditView(intent: Intent) {
    ScrollableColumn {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Attributes", style = MaterialTheme.typography.overline)
            Text(text = "Action", style = MaterialTheme.typography.body2)
            Text(text = "Package name", style = MaterialTheme.typography.body2)
            Text(text = "Class name", style = MaterialTheme.typography.body2)
            Text(text = "Data", style = MaterialTheme.typography.body2)
            Text(text = "Mime type", style = MaterialTheme.typography.body2)
            Text(text = "More info", style = MaterialTheme.typography.overline)
            Text(text = "Flags", style = MaterialTheme.typography.body2)
            Text(text = "Categories", style = MaterialTheme.typography.body2)
            Text(text = "Extras", style = MaterialTheme.typography.overline)
        }
    }
}

@Preview
@Composable
fun PreviewGreeting() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            IntentEditView(intent = Intent(Intent.ACTION_DIAL))
        }
    }
}