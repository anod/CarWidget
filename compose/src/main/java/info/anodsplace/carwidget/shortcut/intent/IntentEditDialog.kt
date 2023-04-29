@file:OptIn(ExperimentalMaterial3Api::class)

package info.anodsplace.carwidget.shortcut.intent

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.ExpandMoreIcon
import info.anodsplace.carwidget.content.R
import info.anodsplace.framework.util.isScalar
import info.anodsplace.framework.util.put
import info.anodsplace.framework.util.putAny
import java.util.*

@Composable
fun EditDialog(confirmText: String, onClose: (Boolean) -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        content()
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { onClose(false) }) {
                Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.close).uppercase(Locale.getDefault()))
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { onClose(true) }) {
                Text(text = confirmText.uppercase(Locale.getDefault()))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraKeyValue(initialKey: String, value: Any?, onValueChange: (String, String, Any?) -> Unit) {
    val (newExtra, setNewExtra) = remember { mutableStateOf(Pair(initialKey, value ?: "")) }
    val enabled by remember { mutableStateOf(value?.isScalar == true) }

    Row {
        OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors( textColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(0.5f),
                value = newExtra.first,
                onValueChange = { value ->
                    setNewExtra(Pair(value, newExtra.second))
                    onValueChange(initialKey, value, newExtra.second)
                },
                label = {
                    Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.key), style = MaterialTheme.typography.titleMedium)
                },
                isError = newExtra.first.isBlank() && newExtra.second.toString().isNotBlank()
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors( textColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(0.5f),
                value = newExtra.second.toString(),
                onValueChange = { value ->
                    if (enabled) {
                        setNewExtra(Pair(newExtra.first, value))
                        onValueChange(initialKey, newExtra.first, value)
                    }
                },
                label = {
                    Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.value), style = MaterialTheme.typography.titleMedium)
                },
                isError = newExtra.first.isBlank() && newExtra.second.toString().isNotBlank()
        )
    }
}

@Composable
fun ExtraEditDialog(initial: IntentField.Extras, onClose: (newExtra: Bundle?) -> Unit) {
    val (newExtra, setNewExtra) = remember { mutableStateOf(initial.bundle) }
    val typeMap: Map<String, Class<out Any>> = newExtra.keySet().associateBy({ it }, { key ->
        val value = newExtra.get(key)
        if (value == null) String::class.java else value::class.java
    })

    AppLog.d("ExtraEditDialog $newExtra")

    EditDialog(
            confirmText = stringResource(id = R.string.add),
            onClose = {
                onClose(if (it) newExtra else null)
                setNewExtra(Bundle())
            }
    ) {
        Text(text = stringResource(id = R.string.extras), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        for (key in newExtra.keySet()) {
            val value = newExtra.get(key)
            ExtraKeyValue(key, value) { initialKey, newKey, newValue ->
                when {
                    newKey.isBlank() -> newExtra.remove(initialKey)
                    newValue is String -> {
                        val type = typeMap[initialKey] ?: String::class.java
                        newExtra.put(newKey, newValue, type)
                    }
                    else -> newExtra.putAny(newKey, newValue)
                }
                setNewExtra(newExtra)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        // New field
        ExtraKeyValue("", "") { initialKey, newKey, newValue ->
            when {
                newKey.isBlank() -> newExtra.remove(initialKey)
                newValue is String -> {
                    val type = typeMap[initialKey] ?: String::class.java
                    newExtra.put(newKey, newValue, type)
                }
                else -> newExtra.putAny(newKey, newValue)
            }
            setNewExtra(newExtra)
        }
    }
}

@Composable
fun ComponentEditDialog(field: IntentField.Component, onClose: (newComponent: ComponentName?) -> Unit) {
    val (newComponent, setNewComponent) = remember { mutableStateOf(field.value) }
    EditDialog(
            confirmText = stringResource(id = R.string.save),
            onClose = { onClose(if (it) newComponent else null) }
    ) {
        Text(text = stringResource(id = R.string.component), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors( textColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth(),
                value = newComponent?.packageName ?: "",
                onValueChange = { value ->
                    setNewComponent(ComponentName(value, newComponent?.className ?: ""))
                },
                label = {
                    Text(text = stringResource(id = R.string.package_name), style = MaterialTheme.typography.titleMedium)
                },
                isError = newComponent?.packageName.isNullOrBlank() && (newComponent?.className?.isNotBlank() == true)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors( textColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth(),
                value = newComponent?.className ?: "",
                onValueChange = {
                    setNewComponent(ComponentName(newComponent?.packageName ?: "", it))
                },
                label = {
                    Text(text = stringResource(id = R.string.class_name), style = MaterialTheme.typography.titleMedium)
                },
                isError = newComponent?.packageName.isNullOrBlank() && (newComponent?.className?.isNotBlank() == true)
        )
    }
}

@Composable
fun FieldEditDialog(title: String, initial: IntentField.StringValue, initialValid: Boolean = true, onSuggestions: () -> Unit, onClick: (IntentField.StringValue?) -> Unit) {
    var field by remember { mutableStateOf(initial) }
    EditDialog(
            confirmText = stringResource(id = R.string.save),
            onClose = { apply -> onClick(if (apply) field else null) }
    ) {
        val isValid = field.isValid.collectAsState(initial = initialValid)
        val isEmpty = field.value.isNullOrEmpty()
        Box {
            OutlinedTextField(
                    colors = TextFieldDefaults.outlinedTextFieldColors( textColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                    value = field.value ?: "",
                    onValueChange = { value ->
                        if (value != field.value) {
                            field = field.copy(value = value)
                        }
                    },
                    label = {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                    },
                    placeholder = {
                        if (field.value.isNullOrEmpty()) {
                            Text(text = stringResource(id = R.string.none), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    isError = !isEmpty && !isValid.value
            )
            if (initial.suggestions != IntentField.Suggestions.None) {
                IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = onSuggestions
                ) {
                    ExpandMoreIcon()
                }
            }
        }

        if (!isEmpty && !isValid.value) {
            Text(
                    text = stringResource(R.string.value_might_be_not_valid),
                    color = MaterialTheme.colorScheme.error,
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
    val editState: IntentField.StringValue = IntentField.Action(Intent.ACTION_DIAL, "Action")
    CarWidgetTheme() {
        Surface {
            FieldEditDialog("Action", editState, initialValid = false, onSuggestions = {}, onClick = { })
        }
    }
}

@Preview("Intent Extra Add Dialog Empty")
@Composable
fun PreviewExtraAddDialogEmpty() {
    val editState = IntentField.Extras(Bundle().apply {
        putString("Banana", null)
        putInt("Int", 325)
        putIntArray("Array", intArrayOf(125, 67, 80))
    })
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        Surface {
            ExtraEditDialog(editState, onClose = { })
        }
    }
}