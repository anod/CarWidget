package info.anodsplace.carwidget.intent

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import androidx.compose.material.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.screens.CarWidgetToolbar
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.compose.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class UpdateField(val field: IntentField) : UiAction.IntentEditAction()

private val DefaultFrontLayerShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.large.copy(topStart = CornerSize(16.dp), topEnd = CornerSize(16.dp))

private val DefaultFrontLayerElevation = 1.dp

private val DefaultFrontLayerScrimColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colors.surface.copy(alpha = 0.60f)

@Composable
fun IntentFieldTitle(text: String) = Text(text = text, style = MaterialTheme.typography.subtitle1)

@Composable
fun IntentFieldValue(value: String?, modifier: Modifier = Modifier) {
    val text = if (value.isNullOrBlank()) stringResource(id = R.string.none) else value
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
        Text(
                text = text,
                style = MaterialTheme.typography.body2,
                modifier = modifier,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IntentInfoRow(icon: ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title)
        Spacer(Modifier.width(8.dp))
        Column {
            IntentFieldTitle(text = title)
            content()
        }
    }
}

@Composable
fun IntentInfoField(icon: ImageVector, field: IntentField.StringValue, modifier: Modifier = Modifier, onClick: (IntentField) -> Unit) {
    IntentInfoRow(
            icon = icon,
            title = field.title,
            modifier = modifier,
            onClick = { onClick(field as IntentField) }
    ) {
        IntentFieldValue(value = field.value, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun IntentExtrasField(intent: Intent, onClick: (IntentField) -> Unit) {
    val items = intent.extras ?: Bundle()
    val extraKeys = items.keySet() ?: emptySet()
    val title = stringResource(id = R.string.extras)
    IntentInfoRow(
            icon = Icons.Filled.FormatListBulleted,
            title = title,
            onClick = { onClick(IntentField.Extras(items)) }
    ) {
        if (extraKeys.isEmpty()) {
            IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            Column(Modifier.padding(vertical = 8.dp)) {
                for (key in extraKeys) {
                    val value = items.get(key)
                    IntentFieldValue(value = "$key: $value", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun IntentComponentField(component: ComponentName?, onClick: (IntentField) -> Unit) {
    IntentInfoRow(
            title = stringResource(id = R.string.component),
            icon = Icons.Filled.LibraryBooks,
            onClick = { onClick(IntentField.Component(component)) }
    ) {
        IntentFieldValue(value = component?.flattenToString(), modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun IntentDetailsView(intent: Intent, modifier: Modifier = Modifier, onItemClick: (IntentField) -> Unit) {
    rememberScrollState(0)
    LazyColumn(modifier = modifier) {
        // use `item` for separate elements like headers
        // and `items` for lists of identical elements
        item {

            IntentInfoField(
                    icon = Icons.Outlined.PlayForWork,
                    field = IntentField.Action(intent.action, stringResource(id = R.string.action)),
                    onClick = onItemClick
            )

            IntentComponentField(
                    component = intent.component,
                    onClick = onItemClick
            )

            IntentInfoField(
                    icon = Icons.Filled.OpenWith,
                    field = IntentField.Data(intent.data, stringResource(id = R.string.data)),
                    onClick = onItemClick
            )

            IntentInfoField(
                    icon = Icons.Filled.Description,
                    field = IntentField.MimeType(intent.type, stringResource(id = R.string.mime_type)),
                    onClick = onItemClick
            )

            IntentInfoRow(
                    icon = Icons.Filled.Flag,
                    title = stringResource(id = R.string.flags),
                    onClick = { onItemClick(IntentField.Flags(intent.flags)) }
            ) {
                val flagNames = intent.flagNames
                if (flagNames.isEmpty()) {
                    IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        for (flagName in flagNames) {
                            IntentFieldValue(value = flagName)
                        }
                    }
                }
            }

            IntentInfoRow(
                    icon = Icons.Filled.Category,
                    title = stringResource(id = R.string.categories),
                    onClick = { onItemClick(IntentField.Categories(intent.categories)) }
            ) {
                val categoryNames = intent.categoryNames
                if (categoryNames.isEmpty()) {
                    IntentFieldValue(value = null, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    for (categoryName in categoryNames) {
                        IntentFieldValue(value = categoryName, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            IntentExtrasField(intent, onClick = onItemClick)
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                    modifier = Modifier.fillMaxWidth(), //.align(Alignment.CenterHorizontally),
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
}

@Composable
fun EditSection(intent: Intent, editState: IntentField, action: MutableSharedFlow<UiAction>, onSuggestions: (IntentField.Suggestions) -> Unit, onClose: () -> Unit) {
    val flagsState = remember(intent.flags) { mutableStateListOf(*intent.flagNames.toTypedArray()) }
    val categoriesState = remember(intent.categories) { mutableStateListOf(*intent.categoryNames.toTypedArray()) }
    val scope = rememberCoroutineScope()
    when (editState) {
        is IntentField.StringValue -> {
            FieldEditDialog(editState.title, editState,
                onSuggestions = { onSuggestions(editState.suggestions) },
                onClick = { newState ->
                    if (newState != null) {
                        scope.launch {
                            action.emit(UpdateField(newState))
                        }
                    }
                    onClose()
                })
        }
        is IntentField.Component -> {
            ComponentEditDialog(editState, onClose = { newComponent ->
                if (newComponent != null) {
                    scope.launch {
                        action.emit(UpdateField(IntentField.Component(newComponent)))
                    }
                }
                onClose()
            })
        }
        is IntentField.Extras -> {
            ExtraEditDialog(editState, onClose = { newBundle ->
                if (newBundle != null) {
                    scope.launch {
                        action.emit(UpdateField(IntentField.Extras(newBundle)))
                    }
                }
                onClose()
            })
        }
        is IntentField.Flags -> {
            val flagsText = stringResource(id = R.string.flags)
            val stateValue = CheckBoxScreenState(
                    flagsText, IntentFlags, flagsState
            )
            CheckBoxScreen(saveText = stringResource(id = R.string.save), stateValue, onDismissRequest = {
                scope.launch {
                    action.emit(UpdateField(IntentField.Flags(flagNamesToInt(flagsState))))
                }
                onClose()
            })
        }
        is IntentField.Categories -> {
            val categoriesText = stringResource(id = R.string.categories)
            val stateValue = CheckBoxScreenState(
                    categoriesText, IntentCategories, categoriesState
            )
            CheckBoxScreen(stringResource(id = R.string.save), stateValue, onDismissRequest = {
                scope.launch {
                    action.emit(UpdateField(IntentField.Categories(categoriesState.mapNotNull { IntentCategories[it] }.toSet())))
                }
                onClose()
            })
        }
        is IntentField.None -> { }
        else -> throw RuntimeException("Unknown field")
    }
}

@Composable
fun IntentEditScreen(
    intent: Flow<Intent>,
    action: MutableSharedFlow<UiAction>,
    addBackPressHandler: Boolean = false,
    initialEditValue: IntentField = IntentField.None,
) {
    val intentState = intent.collectAsState(initial = Intent())
    var editState by remember { mutableStateOf(initialEditValue) }
    val editVisible: Boolean = editState !is IntentField.None
    var suggestionsState by remember { mutableStateOf(IntentField.Suggestions.None) }
    val scope = rememberCoroutineScope()

    if (addBackPressHandler) {
        BackPressHandler(
                onBackPressed = {
                    if (suggestionsState == IntentField.Suggestions.None) {
                        editState = IntentField.None
                    } else {
                        suggestionsState = IntentField.Suggestions.None
                    }
                },
                enabled = editVisible || suggestionsState != IntentField.Suggestions.None
        )
    }

    Scaffold(
            topBar = { CarWidgetToolbar(action) },
            backgroundColor = MaterialTheme.colors.surface,
            content = {
                Column {
                    if (editVisible) {
                        Surface {
                            EditSection(intentState.value, editState, action, onSuggestions = {
                                suggestionsState = it
                            }) {
                                editState = IntentField.None
                            }
                        }
                    }

                    Surface(
                            shape = DefaultFrontLayerShape,
                            elevation = DefaultFrontLayerElevation,
                            color = MaterialTheme.colors.background,
                    ) {
                        Box {
                            IntentDetailsView(intentState.value, modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()) {
                                editState = it
                            }
                            OverlayScrim(
                                    color = DefaultFrontLayerScrimColor,
                                    onDismiss = { editState = IntentField.None },
                                    visible = editVisible
                            )
                        }
                    }
                }

                when (suggestionsState) {
                    IntentField.Suggestions.Action -> {
                        SingleListScreen(
                                state = SingleScreenState(
                                        title = stringResource(id = R.string.action),
                                        items = IntentActions
                                ),
                        ) { _, value ->
                            scope.launch {
                                action.emit(UpdateField(IntentField.Action(value, "")))
                            }
                            suggestionsState = IntentField.Suggestions.None
                        }
                    }
                    IntentField.Suggestions.MimeType -> {
                        SingleListScreen(
                                state = SingleScreenState(
                                        title = stringResource(id = R.string.mime_type),
                                        items = MediaTypes
                                ),
                        ) { _, value ->
                            scope.launch {
                                action.emit(UpdateField(IntentField.MimeType(value, "")))
                            }
                            suggestionsState = IntentField.Suggestions.None
                        }
                    }
                    IntentField.Suggestions.Component -> {

                    }
                    IntentField.Suggestions.None -> { }
                }
            }
    )
}

@Preview("Intent Edit Screen")
@Composable
fun PreviewIntentEdit() {
    CarWidgetTheme() {
        Surface {
            IntentEditScreen(intent = flowOf(Intent(Intent.ACTION_ANSWER)), action = MutableSharedFlow())
        }
    }
}

@Preview("Intent simple edit")
@Composable
fun PreviewIntentSimpleEdit() {
    CarWidgetTheme() {
        Surface {
            IntentEditScreen(
                    intent = flowOf(Intent(Intent.ACTION_ANSWER)),
                    action = MutableSharedFlow(),
                    initialEditValue = IntentField.Action(Intent.ACTION_ANSWER, "Action")
            )
        }
    }
}