package info.anodsplace.carwidget.shortcut.intent

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.outlined.PlayForWork
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntentPolicy
import info.anodsplace.compose.CheckBoxItem
import info.anodsplace.compose.CheckBoxLazyList
import info.anodsplace.compose.OverlayScrim
import info.anodsplace.compose.SingleSelectLazyList
import info.anodsplace.compose.SingleSelectListState

class UpdateField(val field: IntentField)

private val DefaultFrontLayerShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.large.copy(topStart = CornerSize(16.dp), topEnd = CornerSize(16.dp))

private val DefaultFrontLayerScrimColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)

@Composable
fun IntentFieldTitle(text: String) = Text(text = text, style = MaterialTheme.typography.titleMedium)

@Composable
fun IntentFieldValue(value: String?, modifier: Modifier = Modifier) {
    val text = if (value.isNullOrBlank()) stringResource(id = R.string.none) else value
    Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun IntentInfoRow(icon: ImageVector, title: String, modifier: Modifier = Modifier, isEnabled: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick)
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
fun IntentInfoField(
        icon: ImageVector,
        field: IntentField.StringValue,
        modifier: Modifier = Modifier,
        isEnabled: Boolean,
        onClick: (IntentField) -> Unit
) {
    IntentInfoRow(
            icon = icon,
            title = field.title,
            modifier = modifier,
            isEnabled = isEnabled,
            onClick = { onClick(field as IntentField) }
    ) {
        IntentFieldValue(value = field.value, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun IntentExtrasField(
    intent: Intent,
    isEnabled: Boolean,
    onClick: (IntentField) -> Unit
) {
    val items = intent.extras ?: Bundle()
    val extraKeys = items.keySet() ?: emptySet()
    val title = stringResource(id = R.string.extras)
    IntentInfoRow(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            title = title,
            isEnabled = isEnabled,
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
fun IntentComponentField(
        component: ComponentName?,
        isEnabled: Boolean,
        onClick: (IntentField) -> Unit
) {
    IntentInfoRow(
            title = stringResource(id = R.string.component),
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
            isEnabled = isEnabled,
            onClick = { onClick(IntentField.Component(component)) }
    ) {
        IntentFieldValue(value = component?.flattenToString(), modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun IntentDetailsView(
        intent: Intent,
        modifier: Modifier = Modifier,
        onItemClick: (IntentField) -> Unit,
        isReadonly: Boolean
) {
    rememberScrollState(0)
    LazyColumn(modifier = modifier) {
        // use `item` for separate elements like headers
        // and `items` for lists of identical elements
        item {
            IntentInfoField(
                    icon = Icons.Outlined.PlayForWork,
                    field = IntentField.Action(intent.action, stringResource(id = R.string.action)),
                    onClick = onItemClick,
                    isEnabled = !isReadonly
            )

            IntentComponentField(
                    component = intent.component,
                    onClick = onItemClick,
                    isEnabled = !isReadonly
            )

            IntentInfoField(
                    icon = Icons.Filled.OpenWith,
                    field = IntentField.Data(intent.data, stringResource(id = R.string.data)),
                    onClick = onItemClick,
                    isEnabled = !isReadonly
            )

            IntentInfoField(
                    icon = Icons.Filled.Description,
                    field = IntentField.MimeType(intent.type, stringResource(id = R.string.mime_type)),
                    onClick = onItemClick,
                    isEnabled = !isReadonly
            )

            IntentInfoRow(
                    icon = Icons.Filled.Flag,
                    title = stringResource(id = R.string.flags),
                    isEnabled = !isReadonly,
                    onClick = { onItemClick(IntentField.Flags(intent.flags)) },
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
                    isEnabled = !isReadonly,
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

            IntentExtrasField(
                    intent,
                    isEnabled = !isReadonly,
                    onClick = onItemClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                    modifier = Modifier.fillMaxWidth(), //.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(0.2f))
            ) {
                Text(
                        text = intent.toUri(0).toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(16.dp),
                        softWrap = true,
                )
            }
        }
    }
}

@Composable
fun EditSection(
    intent: Intent,
    editState: IntentField,
    onDraftChange: (IntentField.StringValue) -> Unit,
    updateField: (UpdateField) -> Unit,
    onSuggestions: (IntentField.Suggestions) -> Unit,
    allowComponentClear: Boolean,
    onClose: () -> Unit
) {
    when (editState) {
        is IntentField.StringValue -> {
            FieldEditDialog(editState.title, editState,
                onValueChange = onDraftChange,
                onSuggestions = { onSuggestions(editState.suggestions) },
                onClick = { newState ->
                    if (newState != null) {
                        updateField(UpdateField(newState))
                    }
                    onClose()
                })
        }
        is IntentField.Component -> {
            ComponentEditDialog(editState, allowClear = allowComponentClear, onClose = { newComponent ->
                if (newComponent != null) {
                    updateField(UpdateField(IntentField.Component(newComponent)))
                }
                onClose()
            })
        }
        is IntentField.Extras -> {
            ExtraEditDialog(editState, onClose = { newBundle ->
                if (newBundle != null) {
                    updateField(UpdateField(IntentField.Extras(newBundle)))
                }
                onClose()
            })
        }
        is IntentField.Flags -> {
            val flagsItems = remember(intent.flags) {
                editableIntentFlags.map { (key, mask) ->
                    CheckBoxItem(
                        key = key,
                        checked = (intent.flags and mask) == mask,
                        title = key,
                    )
                }
            }

            CheckBoxLazyList(
                items = flagsItems,
                onCheckedChange = { item ->
                    updateField(UpdateField(IntentField.FlagToggle(item.key, item.checked)))
                }
            )
        }
        is IntentField.Categories -> {
            val categoriesItems = remember(intent.categories) {
                editableIntentCategories.map { (key, category) ->
                    CheckBoxItem(
                        key = key,
                        checked = intent.hasCategory(category),
                        title = key,
                    )
                }
            }

            CheckBoxLazyList(
                items = categoriesItems,
                onCheckedChange = { item ->
                    updateField(UpdateField(IntentField.CategoryToggle(item.key, item.checked)))
                }
            )
        }
        is IntentField.None -> { }
        else -> throw RuntimeException("Unknown field")
    }
}

@Composable
fun IntentEditScreen(
    intent: Intent,
    updateField: (UpdateField) -> Unit,
    modifier: Modifier = Modifier,
    initialEditValue: IntentField = IntentField.None,
    isReadonly: Boolean = false,
    allowComponentClear: Boolean = true
) {
    var editState by remember { mutableStateOf(initialEditValue) }
    val editVisible: Boolean = editState !is IntentField.None
    var suggestionsState by remember { mutableStateOf(IntentField.Suggestions.None) }
    val closeEditor: () -> Unit = {
        suggestionsState = IntentField.Suggestions.None
        editState = IntentField.None
    }

    Column(modifier = modifier) {
        if (editVisible) {
            Surface {
                EditSection(
                    intent = intent,
                    editState = editState,
                    onDraftChange = { editState = it },
                    updateField = updateField,
                    onSuggestions = { suggestionsState = it },
                    allowComponentClear = allowComponentClear,
                    onClose = closeEditor
                )
            }
        }

        Surface(
                shape = DefaultFrontLayerShape,
                color = MaterialTheme.colorScheme.background,
        ) {
            Box {
                IntentDetailsView(
                    intent = intent,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    isReadonly = isReadonly,
                    onItemClick = { editState = it }
                )
                OverlayScrim(
                        color = DefaultFrontLayerScrimColor,
                        onDismiss = closeEditor,
                        visible = editVisible
                )
            }
        }
    }

    val suggestions = when (suggestionsState) {
        IntentField.Suggestions.Action -> ShortcutIntentPolicy.actions.associateBy { it }
        IntentField.Suggestions.MimeType -> ShortcutIntentPolicy.mimeTypes.associateBy { it }
        else -> null
    }
    if (suggestions != null) {
        SingleSelectLazyList(state = SingleSelectListState(items = suggestions)) { _, value ->
            val draft = editState as? IntentField.StringValue
            if (draft != null && draft.suggestions == suggestionsState) {
                editState = draft.copy(value)
            }
            suggestionsState = IntentField.Suggestions.None
        }
    }
}

@Preview("Intent Edit Screen")
@Composable
fun PreviewIntentEdit() {
    CarWidgetTheme {
        Surface {
            IntentEditScreen(intent = Intent(Intent.ACTION_ANSWER), updateField = {})
        }
    }
}

@Preview("Intent simple edit")
@Composable
fun PreviewIntentSimpleEdit() {
    CarWidgetTheme {
        Surface {
            IntentEditScreen(
                    intent = Intent(Intent.ACTION_ANSWER),
                    updateField = {},
                    initialEditValue = IntentField.Action(Intent.ACTION_ANSWER, "Action")
            )
        }
    }
}