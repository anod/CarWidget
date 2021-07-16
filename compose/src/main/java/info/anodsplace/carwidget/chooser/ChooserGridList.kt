package info.anodsplace.carwidget.chooser

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import info.anodsplace.compose.PicassoIcon
import info.anodsplace.carwidget.content.graphics.PackageIconRequestHandler
import info.anodsplace.carwidget.utils.SystemIconSize

class Header(title: String, val iconVector: ImageVector) :
    ChooserEntry(null, title)

open class ChooserEntry(
        val componentName: ComponentName?,
        var title: String,
        @DrawableRes val iconRes: Int = 0,
        val icon: Drawable? = null,
        var intent: Intent? = null,
        var extras: Bundle? = null
) {

    constructor(info: ResolveInfo, title: String?):
            this(ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name), title ?: info.activityInfo.name ?: ""
            )

    constructor(title: String, icon: Drawable?)
            : this(componentName = null, title = title, icon = icon)

    constructor(pm: PackageManager, resolveInfo: ResolveInfo)
            : this(
            componentName = ComponentName(
                    resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name
            ),
            title = resolveInfo.loadLabel(pm).toString(),
            icon = resolveInfo.loadIcon(pm)
    )

    /**
     * Build the [Intent] described by this item. If this item
     * can't create a valid [android.content.ComponentName], it
     * will return [Intent.ACTION_CREATE_SHORTCUT] filled with the
     * item label.
     */
    fun getIntent(baseIntent: Intent?): Intent {
        if (this.intent != null) {
            return this.intent!!
        }
        val intent = if (baseIntent != null) {
            Intent(baseIntent)
        } else {
            Intent(Intent.ACTION_MAIN)
        }
        if (componentName != null) {
            // Valid package and class, so fill details as normal intent
            intent.component = componentName
            if (extras != null) {
                intent.putExtras(extras!!)
            }
        } else {
            intent.action = Intent.ACTION_CREATE_SHORTCUT
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
        }
        return intent
    }
}

private fun ChooserEntry.iconUri(context: Context): Uri {
    return if (componentName == null ) {
        if (iconRes != 0) {
            Uri.fromParts(ContentResolver.SCHEME_ANDROID_RESOURCE, context.packageName, iconRes.toString())
        } else Uri.EMPTY
    } else Uri.fromParts(PackageIconRequestHandler.SCHEME, componentName.flattenToShortString(), null)
}

@Composable
fun EntryIcon(entry: ChooserEntry, onClick: (ChooserEntry) -> Unit) {
    val iconModifier = Modifier
        .size(SystemIconSize)
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable { onClick(entry) }
            .padding(8.dp)
            ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (entry is Header) {
            Icon(imageVector = entry.iconVector, contentDescription = null, modifier = iconModifier)
        } else {
            PicassoIcon(entry.iconUri(context), modifier = iconModifier)
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = entry.title,
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooserGridList(headers: List<ChooserEntry>, list: List<ChooserEntry>, onClick: (ChooserEntry) -> Unit) {

    LazyVerticalGrid(
        modifier = Modifier.padding(16.dp),
        cells = GridCells.Adaptive(64.dp),
        state = rememberLazyListState()
    ) {
        items(headers.size) { index ->
            val entry = headers[index]
            EntryIcon(entry, onClick)
        }
        items(list.size) { index ->
            val entry = list[index]
            EntryIcon(entry, onClick)
        }
    }
}

@Composable
fun ChooserDialog(
    appsLoader: AppsListLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = listOf(),
    onDismissRequest: () -> Unit,
    onClick: (ChooserEntry) -> Unit
) {
    val appsList by appsLoader.load().collectAsState(initial = emptyList())
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 352.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface
        ) {
            ChooserGridList(headers, appsList, onClick = onClick)
        }
    }
}