package info.anodsplace.carwidget.iconpack

import android.app.UiModeManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPackScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = info.anodsplace.carwidget.iconpack.R.string.icon_pack_label)) }) }
    ) { inner ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(IconCategories) { cat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(imageVector = cat.icon, contentDescription = null)
                        Text(
                            text = stringResource(id = cat.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(name = "IconPack Light", showBackground = true)
@Composable
private fun IconPackScreenPreviewLight() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_NO) { IconPackScreen() }
}

@Preview(name = "IconPack Dark", showBackground = true)
@Composable
private fun IconPackScreenPreviewDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) { IconPackScreen() }
}