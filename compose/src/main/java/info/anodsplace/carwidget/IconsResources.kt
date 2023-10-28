package info.anodsplace.carwidget

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shop2
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RadioIcon(isChecked: Boolean) {
    Icon(
        imageVector = if (isChecked) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
        contentDescription = null
    )
}

@Composable
fun SettingsIcon(contentDescription: String? = null) {
    Icon(painter = painterResource(id = info.anodsplace.carwidget.skin.R.drawable.ic_holo_settings), contentDescription = contentDescription)
}

@Composable
fun MoreMenuIcon() {
    Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.more))
}

@Composable
fun ExpandRightIcon() {
    Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
}

@Composable
fun ExpandIcon() {
    Icon(imageVector = Icons.Default.ArrowDropUp, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.expand))
}

@Composable
fun CollapseIcon() {
    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.collapse))
}

@Composable
fun ExpandMoreIcon() {
    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.more))
}

@Composable
fun BackArrowIcon() {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.back)
    )
}

@Composable
fun TextIncreaseIcon() {
    Icon(imageVector = Icons.Default.TextIncrease, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.increase))
}

@Composable
fun TextDecreaseIcon() {
    Icon(imageVector = Icons.Default.TextDecrease, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.decrease))
}

@Composable
fun VolumeIncreaseIcon() {
    Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.increase))
}

@Composable
fun VolumeDecreaseIcon() {
    Icon(imageVector = Icons.Default.VolumeDown, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.decrease))
}

@Composable
fun StoreVersionSignIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.Default.Shop2,
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun RefreshIcon() {
    Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.refresh))
}

@Composable
fun CheckIcon() {
    Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(id = android.R.string.ok))
}

@Composable
fun WarningIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.Filled.Warning,
        modifier = modifier,
        tint = tint,
        contentDescription = null)
}

@Composable
fun WidgetsIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.Filled.Widgets,
        modifier = modifier,
        tint = tint,
        contentDescription = null)
}

@Composable
fun DeleteIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.Filled.Delete,
        modifier = modifier,
        tint = tint,
        contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.delete))
}

@Composable
fun EditIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(
        imageVector = Icons.Filled.Edit,
        modifier = modifier,
        tint = tint,
        contentDescription = contentDescription)
}

@Composable
fun InfoIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(
            imageVector = Icons.Outlined.Info,
            modifier = modifier,
            tint = tint,
            contentDescription = contentDescription)
}

@Composable
fun ImageIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(
        imageVector = Icons.Filled.Image,
        modifier = modifier,
        tint = tint,
        contentDescription = contentDescription)
}

@Preview(widthDp = 200)
@Composable
fun IconsResourcesPreview() {
    MaterialTheme {
        Surface {
            val icons = listOf<@Composable () -> Unit>(
                { RadioIcon(isChecked = true) },
                { RadioIcon(isChecked = false) },
                { MoreMenuIcon() },
                { ExpandRightIcon() },
                { BackArrowIcon() },
                { StoreVersionSignIcon() },
                { RefreshIcon() },
                { TextIncreaseIcon() },
                { TextDecreaseIcon() },
                { VolumeIncreaseIcon() },
                { VolumeDecreaseIcon() },
                { SettingsIcon() },
                { CheckIcon() },
                { WarningIcon() },
                { WidgetsIcon() },
                { ExpandIcon() },
                { CollapseIcon() },
                { ExpandMoreIcon() },
                { DeleteIcon() },
                { InfoIcon() }
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 20.dp),
            ) {
                items(icons.size) { index ->
                    icons[index]()
                }
            }
        }
    }
}