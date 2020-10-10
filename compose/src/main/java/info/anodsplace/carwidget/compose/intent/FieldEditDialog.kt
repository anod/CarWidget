package info.anodsplace.carwidget.compose.intent

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.prefs.IntentField
import kotlinx.coroutines.flow.collect
import java.util.*

@Composable
fun EditDialog(confirmText: String, onClick: (Boolean) -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        content()
        Spacer(modifier = Modifier.preferredHeight(16.dp))
        Row {
            Button(onClick = { onClick(false) }) {
                Text(text = stringResource(id = R.string.close).toUpperCase(Locale.getDefault()))
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { onClick(true) }) {
                Text(text = confirmText.toUpperCase(Locale.getDefault()))
            }
        }
    }
}

@Composable
fun ExtraAddDialog(initial: IntentField.Extras, onClick: (Pair<String, String>) -> Unit) {
    val (newExtra, setNewExtra) = remember { mutableStateOf(Pair("", "")) }
    EditDialog(
            confirmText = stringResource(id = R.string.add),
            onClick = {
                onClick(newExtra)
                setNewExtra(Pair("", ""))
            }
    ) {
        Text(text = initial.title, style = MaterialTheme.typography.subtitle2)
        Spacer(modifier = Modifier.preferredHeight(8.dp))

        OutlinedTextField(
                activeColor = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth(),
                value = newExtra.first,
                onValueChange = {
                    setNewExtra(Pair(it, newExtra.second))
                },
                label = {
                    Text(text = stringResource(id = R.string.key), style = MaterialTheme.typography.subtitle1)
                }
        )

        OutlinedTextField(
                activeColor = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth(),
                value = newExtra.first,
                onValueChange = {
                    setNewExtra(Pair(newExtra.first, it))
                },
                label = {
                    Text(text = stringResource(id = R.string.value), style = MaterialTheme.typography.subtitle1)
                }
        )
    }
}

@Composable
fun FieldEditDialog(initial: IntentField.StringValue, initialValid: Boolean = true, onClick: (IntentField.StringValue?) -> Unit) {
    val field = mutableStateOf(initial)
    EditDialog(
            confirmText = stringResource(id = R.string.save),
            onClick = { apply -> onClick(if (apply) field.value else null) }
    ) {
        val isValid = field.value.isValid.collectAsState(initial = initialValid)
        val isEmpty = field.value.value.isNullOrEmpty()
        OutlinedTextField(
                activeColor = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth(),
                value = field.value.value ?: "",
                onValueChange = {
                    if (it != field.value.value) {
                        field.value = field.value.copy(it)
                    }
                },
                label = {
                    Text(text = field.value.title, style = MaterialTheme.typography.subtitle1)
                },
                placeholder = {
                    if (field.value.value.isNullOrEmpty()) {
                        Text(text = stringResource(id = R.string.none), style = MaterialTheme.typography.subtitle1)
                    }
                },
                isErrorValue = !isEmpty && !isValid.value
        )

        if (!isEmpty && !isValid.value) {
            Text(
                    text = stringResource(R.string.value_might_be_not_valid),
                    color = MaterialTheme.colors.error,
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
            )
        }
    }
}

@Preview("Intent Edit Dialog")
@Composable
fun PreviewIntentEditDialog() {
    val editState:  IntentField.StringValue = IntentField.Action(Intent.ACTION_DIAL, "Action")
    CarWidgetTheme(darkTheme = false) {
        Surface {
            FieldEditDialog(editState, initialValid = false, onClick = { })
        }
    }
}

@Preview("Intent Edit Dialog Empty")
@Composable
fun PreviewIntentEditDialogEmpty() {
    val editState: IntentField.StringValue = IntentField.PackageName(null, "Package name")
    CarWidgetTheme(darkTheme = true) {
        Surface {
            FieldEditDialog(editState, onClick = { })
        }
    }
}

@Preview("Intent Extra Add Dialog Empty")
@Composable
fun PreviewExtraAddDialogEmpty() {
    val editState = IntentField.Extras(Bundle.EMPTY, "Add extra")
    CarWidgetTheme(darkTheme = true) {
        Surface {
            ExtraAddDialog(editState, onClick = { })
        }
    }
}