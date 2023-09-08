package info.anodsplace.carwidget.shortcut

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import info.anodsplace.carwidget.BackArrowIcon
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.DeleteIcon
import info.anodsplace.carwidget.ExpandRightIcon
import info.anodsplace.carwidget.InfoIcon
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.toImageRequest
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.shortcut.intent.IntentEditScreen
import info.anodsplace.framework.content.forIconPack
import info.anodsplace.graphics.DrawableUri

@Composable
fun ShortcutEditScreen(
        state: ShortcutEditViewState,
        onEvent: (ShortcutEditViewEvent) -> Unit,
        onDismissRequest: () -> Unit,
        imageLoader: ImageLoader,
        widgetSettings: WidgetInterface = WidgetInterface.NoOp()
) {
    Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 436.dp),
            shape = MaterialTheme.shapes.medium,
    ) {
        if (state.shortcut != null) {
            ShortcutEditContent(
                shortcut = state.shortcut,
                iconVersion = state.iconVersion,
                isIntentReadonly = true,
                onEvent = onEvent,
                onDismissRequest = onDismissRequest,
                expanded = state.expanded,
                imageLoader = imageLoader,
                widgetSettings = widgetSettings
            )
        }
    }

    if (state.showIconPackPicker) {
        val context = LocalContext.current
        val iconPackImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            onEvent(ShortcutEditViewEvent.IconPackResult(
                intent = it.data,
                resolveProperties = DrawableUri.ResolveProperties(
                    maxIconSize = UtilitiesBitmap.getIconMaxSize(context),
                    targetDensity = UtilitiesBitmap.getTargetDensity(context),
                    context = context
                )
            ))
        }
        IntentChooser(
            intent = Intent().forIconPack(),
            onChoose = {
                iconPackImage.launch(Intent().apply { component = it.componentName }.forIconPack())
            },
            onDismissRequest = { onEvent(ShortcutEditViewEvent.IconPackPicker(show = false)) },
            imageLoader = imageLoader
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutEditContent(
        shortcut: Shortcut,
        iconVersion: Int,
        expanded: Boolean,
        isIntentReadonly: Boolean,
        onEvent: (ShortcutEditViewEvent) -> Unit,
        onDismissRequest: () -> Unit,
        imageLoader: ImageLoader,
        widgetSettings: WidgetInterface = WidgetInterface.NoOp()
) {
    Column {
        TopAppBar(
            title = { Text(text = stringResource(id = if (expanded && isIntentReadonly) R.string.intent else R.string.shortcut_edit_title)) },
            navigationIcon = {
                IconButton(onClick = {
                    if (expanded) {
                        onEvent(ShortcutEditViewEvent.ToggleAdvanced(expanded = false))
                    } else {
                        onDismissRequest()
                    }
                }) {
                    BackArrowIcon()
                }
            }
        )
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f, fill = false)
                .let {
                    if (expanded) it else it.verticalScroll(scrollState)
                }
        ) {
            if (!expanded) {
                ShortcutDetails(
                    shortcut = shortcut,
                    iconVersion = iconVersion,
                    widgetSettings = widgetSettings,
                    imageLoader = imageLoader,
                    onEvent = onEvent,
                    onDismissRequest = onDismissRequest
                )
            } else {
                IntentEditScreen(
                    intent = shortcut.intent,
                    updateField = { onEvent(ShortcutEditViewEvent.UpdateField(it.field)) },
                    modifier = Modifier,
                    isReadonly = isIntentReadonly
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            Button(onClick = {
                onDismissRequest()
            }, modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(text = "OK")
            }
        }
    }
}

@Composable
private fun ShortcutDetails(
    shortcut: Shortcut,
    iconVersion: Int,
    widgetSettings: WidgetInterface,
    imageLoader: ImageLoader,
    onEvent: (ShortcutEditViewEvent) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val customImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        onEvent(ShortcutEditViewEvent.CustomIconResult(
            uri = it,
            resolveProperties = DrawableUri.ResolveProperties(
                maxIconSize = UtilitiesBitmap.getIconMaxSize(context),
                targetDensity = UtilitiesBitmap.getTargetDensity(context),
                context = context
            )
        ))
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(96.dp),
    ) {
        AsyncImage(
            model = shortcut.toImageRequest(LocalContext.current, widgetSettings.adaptiveIconStyle, iconVersion = iconVersion),
            contentDescription = shortcut.title.toString(),
            imageLoader = imageLoader,
            modifier = Modifier
                .size(96.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.medium
                )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Card(
            modifier = Modifier
                .height(96.dp)
                .weight(1.0f),
            border = cardBorder(),
        ) {
            SectionHeader {
                Text(text = stringResource(R.string.edit_title), modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                value = shortcut.title.toString(),
                shape = MaterialTheme.shapes.medium,
                onValueChange = {},
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cardBorderColor,
                        unfocusedBorderColor = cardBorderColor,
                )
            )
        }
    }
    SectionCard {
        SectionHeader {
            Text(text = stringResource(R.string.customize_icon))
        }
        SectionCard(onClick = { customImage.launch("image/*") }) {
            SectionAction {
                Text(text = stringResource(R.string.icon_custom))
            }
        }
        SectionCard(onClick = { onEvent(ShortcutEditViewEvent.IconPackPicker(show = true)) }) {
            SectionAction {
                Text(text = stringResource(R.string.icon_adw_icon_pack))
            }
        }
        if (shortcut.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && shortcut.isCustomIcon) {
            SectionCard(onClick = { onEvent(ShortcutEditViewEvent.DefaultIconReset) }) {
                SectionAction {
                    Text(text = stringResource(R.string.icon_default))
                }
            }
        }
    }
    SectionCard(onClick = {
        onEvent(ShortcutEditViewEvent.Drop)
        onDismissRequest()
    }) {
        SectionAction {
            DeleteIcon()
            Text(text = stringResource(R.string.delete))
        }
    }
    SectionCard(onClick = { onEvent(ShortcutEditViewEvent.ToggleAdvanced(expanded = true)) }) {
        SectionAction {
            InfoIcon(contentDescription = stringResource(id = R.string.intent))
            Text(text = stringResource(id = R.string.intent))
            Spacer(modifier = Modifier.weight(1.0f))
            ExpandRightIcon()
        }
    }
}

@Composable
private fun SectionCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, onClickLabel: String? = null, content: @Composable ColumnScope.() -> Unit = {}) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .let {
                if (onClick != null) {
                    it.clickable(onClick = onClick, onClickLabel = onClickLabel)
                } else {
                    it
                }
            }
        ,
        border = cardBorder(),
        content = content
    )
}

private val cardBorderColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

@Composable
private fun cardBorder(color: Color = cardBorderColor) = BorderStroke(1.dp, color)

@Composable
private fun SectionAction(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit = {}) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
        ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
            Row(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
private fun SectionHeader(content: @Composable RowScope.() -> Unit = {}) {
    ProvideTextStyle(value = MaterialTheme.typography.labelMedium) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Preview("Preview Shortcut Edit")
@Composable
fun PreviewShortcutEditContent() {
    CarWidgetTheme {
        ShortcutEditScreen(
            state = ShortcutEditViewState(
                shortcut = Shortcut(0,0, 0, "Title", false, Intent())
            ),
            onEvent = { },
            onDismissRequest = { },
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        )
    }
}