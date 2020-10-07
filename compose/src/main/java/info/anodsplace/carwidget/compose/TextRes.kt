package info.anodsplace.carwidget.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class TextRes(val value: String)

@Composable
fun TextRes(@StringRes id: Int, text: String? = null): TextRes {
    return if (id != 0)
        TextRes(stringResource(id = id))
    else
        TextRes(text ?: "")
}