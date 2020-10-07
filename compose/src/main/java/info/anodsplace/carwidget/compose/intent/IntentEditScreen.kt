package info.anodsplace.carwidget.compose.intent

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayForWork
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.TextRes
import info.anodsplace.carwidget.compose.UiAction
import info.anodsplace.framework.livedata.SingleLiveEvent

object AddExtra : UiAction.IntentEditAction()

@Composable
fun IntentFieldTitle(text: TextRes) = Text(text = text.value, style = MaterialTheme.typography.subtitle1)

@Composable
fun IntentFieldValue(value: String?, modifier: Modifier = Modifier) {
    val text = if (value.isNullOrBlank()) stringResource(id = R.string.none) else value
    ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
        Text(text = text, style = MaterialTheme.typography.body2, modifier = modifier)
    }
}

@Composable
fun IntentInfoRow(icon: VectorAsset, title: TextRes, modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(asset = icon)
        Spacer(Modifier.preferredWidth(8.dp))
        Column {
            IntentFieldTitle(text = title)
            content()
        }
    }
}

@Composable
fun IntentInfoField(icon: VectorAsset, title: TextRes, value: String?, type: Int, modifier: Modifier = Modifier, onClick: (EditSheetState) -> Unit) {
    val (editState, _) = mutableStateOf(EditSheetState(title.value, value = value, type = type))
    IntentInfoRow(
            icon = icon,
            title = title,
            modifier = modifier,
            onClick = { onClick(editState) }
    ) {
        IntentFieldValue(value = value, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun IntentExtras(intent: Intent, extras: LiveData<Bundle>, action: SingleLiveEvent<UiAction>) {
    val items = extras.observeAsState(initial = intent.extras ?: Bundle.EMPTY)
    val extraKeys = items.value.keySet() ?: emptySet()
    if (extraKeys.isEmpty()) {
        IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
    } else {
        for (key in extraKeys) {
            val value = items.value.get(key)
            ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                Text(text = "$key: $value", style = MaterialTheme.typography.body2)
            }
        }
    }
    Spacer(Modifier.preferredWidth(8.dp))
    Button(onClick = { action.value = AddExtra }) {
        Icon(Icons.Filled.Add)
        Text(text = stringResource(id = R.string.add_extra), style = MaterialTheme.typography.overline)
    }
}

@Composable
fun IntentEditView(intent: Intent, extras: LiveData<Bundle>, action: SingleLiveEvent<UiAction>, onItemClick: (EditSheetState) -> Unit) {
    ScrollableColumn(Modifier.padding(16.dp).fillMaxSize()) {
        IntentInfoField(
                icon = Icons.Outlined.PlayForWork,
                title = TextRes(id = R.string.action),
                value = intent.action,
                type = 0,
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.LibraryBooks,
                title = TextRes(id = R.string.package_name),
                value = intent.component?.packageName,
                type = 1,
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.Article,
                title = TextRes(id = R.string.class_name),
                value = intent.component?.className,
                type = 2,
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.OpenWith,
                title = TextRes(id = R.string.data),
                value = intent.data?.toString(),
                type = 3,
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.Description,
                title = TextRes(id = R.string.mime_type),
                value = intent.type,
                type = 4,
                onClick = onItemClick
        )
        IntentInfoRow(
                icon = Icons.Filled.Category,
                title = TextRes(id = R.string.categories),
                onClick = { }
        ) {
            if (intent.categories.isNullOrEmpty()) {
                IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                IntentFieldValue(value = intent.categories?.joinToString()
                        ?: "", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        IntentInfoRow(
                icon = Icons.Filled.FormatListBulleted,
                title = TextRes(id = R.string.extras),
                onClick = { }
        ) {
            IntentExtras(intent, extras, action)
        }
    }
}

data class EditSheetState(val title: String, var value: String?, val type: Int)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IntentEditScreen(intent: Intent, extras: LiveData<Bundle>, action: SingleLiveEvent<UiAction>) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val (editState, setEditState) = remember { mutableStateOf(EditSheetState("", "", 0)) }
    BottomSheetScaffold(
            sheetContent = {
                FieldEditDialog(editState, onClick = {
                    scaffoldState.bottomSheetState.collapse()
                })
            },
            sheetShape = RoundedCornerShape(
                    topLeft = 4.dp,
                    topRight = 4.dp,
                    bottomLeft = 0.dp,
                    bottomRight = 0.dp
            ),
            sheetPeekHeight = 0.dp,
            scaffoldState = scaffoldState,
            topBar = {
                info.anodsplace.carwidget.compose.AppBar(action)
            },
            bodyContent = {
                IntentEditView(intent, extras, action) {
                    setEditState(it)
                    scaffoldState.bottomSheetState.expand()
                }
            }
    )
}

@Preview("Intent Edit Screen")
@Composable
fun PreviewIntentEdit() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            IntentEditScreen(intent = Intent(Intent.ACTION_DIAL), extras = MutableLiveData(), action = SingleLiveEvent())
        }
    }
}

@Preview("Intent Edit Screen2")
@Composable
fun PreviewIntentEdit2() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            IntentEditView(intent = Intent(Intent.ACTION_DIAL), extras = MutableLiveData(), action = SingleLiveEvent()) { }
        }
    }
}
