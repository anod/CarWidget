package info.anodsplace.carwidget.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CheckIcon
import info.anodsplace.carwidget.navigation.SceneNavKey
import info.anodsplace.carwidget.navigation.TabNavKey

@Composable
fun BottomTabsMenu(items: Set<TabNavKey>, currentTab: TabNavKey?, onClick: (TabNavKey) -> Unit) {
    NavigationBar {
        items.forEachIndexed { _, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringResource(id = item.title),
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentTab == item,
                onClick = {
                    onClick(item)
                }
            )
        }
    }
}

@Composable
fun NavRailMenu(
    items: Set<TabNavKey>,
    showApply: Boolean,
    currentRoute: SceneNavKey?,
    onClick: (SceneNavKey) -> Unit,
    onApply: () -> Unit,
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 3.0.dp
    ) {
        NavigationRail(
            header = {
                if (showApply) {
                    IconButton(onClick = onApply) {
                        CheckIcon()
                    }
                }
            },
            windowInsets = windowInsets
        ) {
            Spacer(Modifier.weight(1f))
            items.forEachIndexed { _, item ->
                NavigationRailItem(
                    selected = currentRoute == item,
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = {
                        Text(
                            text = stringResource(id = item.title),
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    },
                    alwaysShowLabel = false,
                    onClick = {
                        onClick(item as SceneNavKey)
                    }
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }

}