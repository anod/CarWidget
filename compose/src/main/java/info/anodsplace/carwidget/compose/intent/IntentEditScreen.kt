package info.anodsplace.carwidget.compose.intent

import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayForWork
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.prefs.*
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.livedata.SingleLiveEvent

class UpdateField(val field: IntentField) : UiAction.IntentEditAction()

@Composable
fun IntentFieldTitle(text: String) = Text(text = text, style = MaterialTheme.typography.subtitle1)

@Composable
fun IntentFieldValue(value: String?, modifier: Modifier = Modifier) {
    val text = if (value.isNullOrBlank()) stringResource(id = R.string.none) else value
    ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
        Text(text = text, style = MaterialTheme.typography.body2, modifier = modifier)
    }
}

@Composable
fun IntentInfoRow(icon: VectorAsset, title: String, modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    AppLog.d("[IntentEditScreen] IntentInfoRow $title pass")
    Row(modifier
            .fillMaxWidth()
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
            title = field.title,
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
    val title = stringResource(id = R.string.extras)
    IntentInfoRow(
            icon = Icons.Filled.FormatListBulleted,
            title = title,
            onClick = { onClick(IntentField.Extras(items, title)) }
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
fun IntentEditView(intent: Intent, modifier: Modifier = Modifier, onItemClick: (IntentField) -> Unit) {
    ScrollableColumn(modifier) {

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
                title = stringResource(id = R.string.flags),
                onClick = { onItemClick(IntentField.Flags(intent.flags, "")) }
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
                onClick = { onItemClick(IntentField.Categories(intent.categories, "")) }
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

@Composable
fun EditSection(intent: Intent, editState: IntentField, action: SingleLiveEvent<UiAction>, onClose: () -> Unit) {
    val flagsState = remember(intent.flags) { mutableStateListOf(*intent.flagNames.toTypedArray()) }
    val categoriesState =remember(intent.categories) {  mutableStateListOf(*intent.categoryNames.toTypedArray()) }

    AppLog.d("[IntentEditScreen] EditSection pass")
        when (editState) {
            is IntentField.StringValue -> {
                FieldEditDialog(editState, onClick = { newState ->
                    if (newState != null) {
                        action.value = UpdateField(newState)
                    }
                    onClose()
                })
            }
            is IntentField.Extras -> {
                ExtraAddDialog(editState, onClick = {
                    // action.value = UpdateField(editState.value)
                    onClose()
                })
            }
            is IntentField.Flags -> {
                val flagsText = stringResource(id = R.string.flags)
                val stateValue = CheckBoxScreenState(
                        flagsText, IntentFlags, flagsState
                )
                CheckBoxScreen(stateValue, onDismissRequest = {
                    onClose()
                })
            }
            is IntentField.Categories -> {
                val categoriesText = stringResource(id = R.string.categories)
                val stateValue = CheckBoxScreenState(
                        categoriesText, IntentCategories, categoriesState
                )
                CheckBoxScreen(stateValue, onDismissRequest = {
                    onClose()
                })
            }
            is IntentField.None -> { }
            else -> throw RuntimeException("Unknown field")
        }
    }

@Composable
val DefaultFrontLayerShape: Shape
    get() = MaterialTheme.shapes.large
            .copy(topLeft = CornerSize(16.dp), topRight = CornerSize(16.dp))

val DefaultFrontLayerElevation = 1.dp

@Composable
val DefaultFrontLayerScrimColor: Color
    get() = MaterialTheme.colors.surface.copy(alpha = 0.60f)

@Composable
fun IntentEditScreen(
        intent: LiveData<Intent>,
        action: SingleLiveEvent<UiAction>,
        initialEditValue: IntentField = IntentField.None()
) {
    val intentState = intent.observeAsState(Intent())
    var editState by remember { mutableStateOf(initialEditValue) }

    AppLog.d("[IntentEditScreen] layout pass")

    val editVisible: Boolean = editState !is IntentField.None
    backPressHandler(
        onBackPressed = { editState = IntentField.None() },
        enabled = editVisible
    )

    Scaffold(
            topBar = {
                //if (!editVisible) {
                    CarWidgetToolbar(action)
                //}
            },
            backgroundColor = MaterialTheme.colors.surface,
            bodyContent = {
                Column {
                    if (editVisible) {
                        Surface {
                            EditSection(intentState.value, editState, action) {
                                editState = IntentField.None()
                            }
                        }
                    }

                    Surface(
                            shape = DefaultFrontLayerShape,
                            elevation = DefaultFrontLayerElevation,
                            color = MaterialTheme.colors.background,
                    ) {
                        androidx.compose.foundation.layout.Box() {
                            IntentEditView(intentState.value, modifier = Modifier.padding(16.dp).fillMaxSize()) {
                                editState = it
                            }
                            OverlayScrim(
                                    color = DefaultFrontLayerScrimColor,
                                    onDismiss = { editState = IntentField.None() },
                                    visible = editVisible
                            )
                        }
                    }
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

@OptIn(ExperimentalMaterialApi::class)
@Preview("Intent simple edit")
@Composable
fun PreviewIntentSimpleEdit() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            IntentEditScreen(
                    intent = MutableLiveData(Intent(Intent.ACTION_ANSWER)),
                    action = SingleLiveEvent(),
                    initialEditValue = IntentField.Action(Intent.ACTION_ANSWER, "Action")
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview("Intent checkbox edit")
@Composable
fun PreviewIntentCheckboxEdit() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            IntentEditScreen(
                    intent = MutableLiveData(Intent(Intent.ACTION_ANSWER)),
                    action = SingleLiveEvent(),
                    initialEditValue = IntentField.Categories(setOf(), "Categories")
            )
        }
    }
}