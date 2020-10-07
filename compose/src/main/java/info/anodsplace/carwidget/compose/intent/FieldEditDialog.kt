package info.anodsplace.carwidget.compose.intent

import android.content.Intent
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.framework.livedata.SingleLiveEvent

@Composable
fun FieldEditDialog(editState: EditSheetState, onClick: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        TextField(
                modifier = Modifier.fillMaxWidth(),
                value = editState.value ?: "",
                onValueChange = { editState.value = it },
                label = {
                    Text(text = editState.title, style = MaterialTheme.typography.subtitle1)
                },
                placeholder = {
                    Text(text = editState.title, style = MaterialTheme.typography.subtitle1)
                }
        )
        Spacer(modifier = Modifier.preferredHeight(16.dp))
        Row {
            Button(onClick = onClick) {
                Text(text = "CLOSE")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onClick) {
                Text(text = "SAVE")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview("Intent Edit Dialog")
@Composable
fun PreviewIntentEditDialog() {
    val editSheetState = rememberBottomSheetScaffoldState(
            bottomSheetState = BottomSheetState(
                    initialValue = BottomSheetValue.Expanded,
                    clock = AnimationClockAmbient.current
            )
    )
    val (editState, _) = remember { mutableStateOf(EditSheetState("Action", Intent.ACTION_DIAL, 0)) }
    CarWidgetTheme(darkTheme = false) {
        BottomSheetScaffold(
                sheetContent = {
                    FieldEditDialog(editState, onClick = { })
                },
                scaffoldState = editSheetState,
                sheetPeekHeight = 0.dp,
                topBar = {
                    info.anodsplace.carwidget.compose.AppBar(SingleLiveEvent())
                },
                bodyContent = {

                }
        )
    }
}