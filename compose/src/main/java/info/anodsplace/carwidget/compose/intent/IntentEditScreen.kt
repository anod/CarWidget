package info.anodsplace.carwidget.compose.intent

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayForWork
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.toHexString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.TextRes
import info.anodsplace.carwidget.compose.UiAction
import info.anodsplace.carwidget.prefs.*
import info.anodsplace.framework.livedata.SingleLiveEvent

class UpdateField(val field: IntentField) : UiAction.IntentEditAction()

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
fun IntentInfoField(icon: VectorAsset, field: IntentField.StringValue, modifier: Modifier = Modifier, onClick: (IntentField) -> Unit) {
    IntentInfoRow(
            icon = icon,
            title = TextRes(field.title),
            modifier = modifier,
            onClick = { onClick(field as IntentField) }
    ) {
        IntentFieldValue(value = field.value, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun IntentExtrasField(intent: Intent, onClick: (IntentField) -> Unit) {
    val items = intent.extras ?: Bundle.EMPTY
    val extraKeys = items.keySet() ?: emptySet()
    val title = TextRes(id = R.string.extras)
    IntentInfoRow(
            icon = Icons.Filled.FormatListBulleted,
            title = title,
            onClick = { onClick(IntentField.Extras(items, title.value)) }
    ) {
        if (extraKeys.isEmpty()) {
            IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            for (key in extraKeys) {
                val value = items.get(key)
                IntentFieldValue(value = "$key: $value", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun IntentEditView(intent: Intent, action: SingleLiveEvent<UiAction>, onItemClick: (IntentField) -> Unit) {
    ScrollableColumn(Modifier.padding(16.dp).fillMaxSize()) {

        IntentInfoField(
                icon = Icons.Outlined.PlayForWork,
                field = IntentField.Action(intent.action, stringResource(id = R.string.action)),
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.LibraryBooks,
                field = IntentField.PackageName(intent.component?.packageName, stringResource(id = R.string.package_name)),
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.Article,
                field = IntentField.ClassName(intent.component?.className, stringResource(id = R.string.class_name)),
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.OpenWith,
                field = IntentField.Data(intent.data?.toString(), stringResource(id = R.string.data)),
                onClick = onItemClick
        )
        IntentInfoField(
                icon = Icons.Filled.Description,
                field = IntentField.MimeType(intent.type, stringResource(id = R.string.mime_type)),
                onClick = onItemClick
        )
        IntentInfoRow(
                icon = Icons.Filled.Flag,
                title = TextRes(id = R.string.flags),
                onClick = { }
        ) {
            if (intent.flags == 0) {
                IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                IntentFieldValue(value = intent.flags.toHexString(), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        IntentInfoRow(
                icon = Icons.Filled.Category,
                title = TextRes(id = R.string.categories),
                onClick = { }
        ) {
            if (intent.categories.isNullOrEmpty()) {
                IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                for (category in intent.categories) {
                    IntentFieldValue(value = category, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
        IntentExtrasField(intent, onClick = onItemClick)

        Spacer(modifier = Modifier.preferredHeight(16.dp))

        Surface(
                modifier = Modifier.fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.surface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground.copy(0.2f))
        ) {
            Text(
                    text = intent.toUri(0).toString(),
                    style = MaterialTheme.typography.overline,
                    modifier = Modifier.padding(16.dp),
                    softWrap = true
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IntentEditScreen(intent: LiveData<Intent>, action: SingleLiveEvent<UiAction>) {
    val intentState = intent.observeAsState(Intent())
    val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val editState: MutableState<IntentField> = remember { mutableStateOf(IntentField.Action("", "")) }

    BackdropScaffold(
            scaffoldState = scaffoldState,
            appBar = { info.anodsplace.carwidget.compose.AppBar(action) },
            backLayerContent = {
                when (editState.value) {
                    is IntentField.StringValue -> {
                        FieldEditDialog(editState as MutableState<IntentField.StringValue>, onClick = {
                            scaffoldState.conceal()
                        })
                    }
                    is IntentField.Extras -> {
                        ExtraAddDialog(editState as MutableState<IntentField.Extras>, onClick = {
                            scaffoldState.conceal()
                        })
                    }
                    else -> throw RuntimeException("Unknown field")
                }
            },
            frontLayerBackgroundColor = MaterialTheme.colors.background,
            frontLayerContent = {
                IntentEditView(intentState.value, action) {
                    editState.value = it
                    scaffoldState.reveal()
                }
            }
    )
}

@Preview("Intent Edit Screen")
@Composable
fun PreviewIntentEdit() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            IntentEditScreen(intent = MutableLiveData(Intent(Intent.ACTION_ANSWER)), action = SingleLiveEvent())
        }
    }
}

@Preview("Intent Edit Screen Dark")
@Composable
fun PreviewIntentEditDark() {
    CarWidgetTheme(darkTheme = true) {
        Surface {
            IntentEditScreen(intent = MutableLiveData(Intent(Intent.ACTION_ANSWER)), action = SingleLiveEvent())
        }
    }
}