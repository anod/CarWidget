package info.anodsplace.carwidget.compose.intent

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.prefs.IntentField
import java.util.*

@Composable
fun EditDialog(confirmText: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        content()
        Spacer(modifier = Modifier.preferredHeight(16.dp))
        Row {
            Button(onClick = onClick) {
                Text(text = stringResource(id = R.string.close).toUpperCase(Locale.getDefault()))
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onClick) {
                Text(text = confirmText.toUpperCase(Locale.getDefault()))
            }
        }
    }
}

@Composable
fun ExtraAddDialog(state: MutableState<IntentField.Extras>, onClick: (Pair<String, String>) -> Unit) {
    val (newExtra, setNewExtra) = remember { mutableStateOf(Pair("", "")) }
    EditDialog(
            confirmText = stringResource(id = R.string.add),
            onClick = {
                onClick(newExtra)
                setNewExtra(Pair("", ""))
            }
    ) {
        Text(text = state.value.title, style = MaterialTheme.typography.subtitle2)
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
fun FieldEditDialog(state: MutableState<IntentField.StringValue>, onClick: () -> Unit) {
    EditDialog(
            confirmText = stringResource(id = R.string.save),
            onClick = onClick
    ) {
        OutlinedTextField(
                activeColor = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth(),
                value = state.value.value ?: "",
                onValueChange = {
                    if (it != state.value.value) {
                        state.value = state.value.copy(it)
                    }
                },
                label = {
                    Text(text = state.value.title, style = MaterialTheme.typography.subtitle1)
                },
                placeholder = {
                    if (state.value.value.isNullOrEmpty()) {
                        Text(text = stringResource(id = R.string.none), style = MaterialTheme.typography.subtitle1)
                    }
                }
        )
    }
}

@Preview("Intent Edit Dialog")
@Composable
fun PreviewIntentEditDialog() {
    val editState: MutableState<IntentField.StringValue> = remember { mutableStateOf(IntentField.Action(Intent.ACTION_DIAL, "Action")) }
    CarWidgetTheme(darkTheme = false) {
        Surface {
            FieldEditDialog(editState, onClick = { })
        }
    }
}

@Preview("Intent Edit Dialog Empty")
@Composable
fun PreviewIntentEditDialogEmpty() {
    val editState: MutableState<IntentField.StringValue> = remember { mutableStateOf(IntentField.PackageName(null, "Package name")) }
    CarWidgetTheme(darkTheme = true) {
        Surface {
            FieldEditDialog(editState, onClick = { })
        }
    }
}

@Preview("Intent Extra Add Dialog Empty")
@Composable
fun PreviewExtraAddDialogEmpty() {
    val editState: MutableState<IntentField.Extras> = remember { mutableStateOf(IntentField.Extras(Bundle.EMPTY, "Add extra")) }
    CarWidgetTheme(darkTheme = true) {
        Surface {
            ExtraAddDialog(editState, onClick = { })
        }
    }
}